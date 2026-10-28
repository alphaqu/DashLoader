package dev.notalpha.dashloader.client.ui.widget;

import dev.notalpha.dashloader.misc.TranslationHelper;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ConfigListWidget extends ContainerObjectSelectionList<ConfigListWidget.Entry> {
	public static final int INPUT_FIELD_WIDTH = 75;
	public static final int RESET_BUTTON_WIDTH = 50;
	private final TranslationHelper translations = TranslationHelper.getInstance();

	public ConfigListWidget(Minecraft minecraftClient, int i, int j, int k, int l) {
		super(minecraftClient, i, j, k, l);
	}

	@SuppressWarnings("UnusedReturnValue")
	public int addCategory(String label) {
		return addEntry(new CategoryEntry(label));
	}

	@SuppressWarnings("UnusedReturnValue")
	public int addBoolToggle(String label, boolean value, boolean defaultValue, BooleanConsumer saveCallback) {
		return addEntry(new BoolConfigEntry(label, value, defaultValue, saveCallback));
	}

	@SuppressWarnings("UnusedReturnValue")
	public int addIntSlider(String label, int value, int defaultValue, int min, int max, IntConsumer saveCallback) {
		return addEntry(new IntSliderConfigEntry(label, value, defaultValue, min, max, saveCallback));
	}

	@SuppressWarnings("UnusedReturnValue")
	public int addTextField(String label, String value, String defaultValue, Consumer<String> saveCallback) {
		return addEntry(new TextFieldEntry(label, value, defaultValue, c -> true, saveCallback));
	}

	@SuppressWarnings("UnusedReturnValue")
	public int addIntField(String label, int value, int defaultValue, IntConsumer saveCallback) {
		return addEntry(new IntFieldEntry(label, value, defaultValue, saveCallback));
	}

	public void saveValues() {
		this.children().forEach(child -> {
			if (child instanceof ConfigEntry<?> entry) {
				entry.saveValue();
			}
		});
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	public void update() {
		this.children().forEach(dev.notalpha.dashloader.client.ui.widget.ConfigListWidget.Entry::update);
	}

	abstract class Entry extends ContainerObjectSelectionList.Entry<dev.notalpha.dashloader.client.ui.widget.ConfigListWidget.Entry> {
		public Component label;

		Entry(String label) {
			this.label = Component.nullToEmpty(translations.get(label));
		}

		void update() {
		}
	}

	class CategoryEntry extends dev.notalpha.dashloader.client.ui.widget.ConfigListWidget.Entry {
		CategoryEntry(String label) {
			super(label);
		}

		@Override
		public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			final var textRenderer = ConfigListWidget.this.minecraft.font;
			int entryWidth = this.getWidth();
			int entryHeight = this.getHeight();
			context.text(
					textRenderer,
					this.label,
					(ConfigListWidget.this.width - textRenderer.width(label)) / 2,
					this.getY() + entryHeight - ConfigListWidget.this.minecraft.font.lineHeight - 1,
					0xFFFFFFFF,
					false
			);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}
	}

	abstract class ConfigEntry<T> extends dev.notalpha.dashloader.client.ui.widget.ConfigListWidget.Entry {
		static final Component RESET_TEXT = Component.translatable("controls.reset");
		protected final T defaultValue;
		public AbstractWidget widget;
		public Button resetButton;
		protected T value;
		protected Tooltip tooltip;
		protected Consumer<T> saveFunc;

		ConfigEntry(String label, T value, T defaultValue, Consumer<T> saveCallback) {
			super(label);
			this.value = value;
			this.defaultValue = defaultValue;
			this.saveFunc = saveCallback;
			if (translations.has(label + ".tooltip")) {
				this.tooltip = Tooltip.create(Component.nullToEmpty(translations.get(label + ".tooltip")));
			}

			this.resetButton = new Button.Builder(RESET_TEXT, button -> {
				this.value = this.defaultValue;
				this.updateWidgetText();
				ConfigListWidget.this.update();
			}).width(RESET_BUTTON_WIDTH).build();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int x = this.getX();
			int y = this.getY();
			int entryWidth = this.getWidth();
			int entryHeight = this.getHeight();
			context.text(
					ConfigListWidget.this.minecraft.font,
					this.label,
					x,
					y + (entryHeight - ConfigListWidget.this.minecraft.font.lineHeight) / 2,
					0xFFFFFFFF,
					false
			);

			this.widget.setPosition(x + entryWidth - INPUT_FIELD_WIDTH - RESET_BUTTON_WIDTH - 5, y - 2);
			this.widget.extractRenderState(context, mouseX, mouseY, tickDelta);
			this.resetButton.setPosition(x + entryWidth - RESET_BUTTON_WIDTH, y - 2);
			this.resetButton.extractRenderState(context, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(this.widget, this.resetButton);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(this.widget, this.resetButton);
		}

		@Override
		void update() {
			this.resetButton.active = !Objects.equals(this.value, this.defaultValue);
		}

		public T getValue() {
			return this.value;
		}

		public void saveValue() {
			this.saveFunc.accept(this.value);
		}

		abstract void updateWidgetText();
	}

	class BoolConfigEntry extends ConfigEntry<Boolean> {
		BoolConfigEntry(String label, boolean value, boolean defaultValue, BooleanConsumer saveCallback) {
			super(label, value, defaultValue, saveCallback);

			this.widget = new Button.Builder(CommonComponents.optionStatus(this.value), button -> {
				this.value = !(boolean) this.value;
				updateWidgetText();
				ConfigListWidget.this.update();
			}).width(INPUT_FIELD_WIDTH).build();

			this.widget.setTooltip(this.tooltip);
		}

		@Override
		void updateWidgetText() {
			this.widget.setMessage(CommonComponents.optionStatus(this.value));
		}
	}

	class IntSliderConfigEntry extends ConfigEntry<Integer> {
		private final int min;
		private final int max;

		IntSliderConfigEntry(String label, int value, int defaultValue, int min, int max, IntConsumer saveCallback) {
			super(label, value, defaultValue, saveCallback);
			this.min = min;
			this.max = max;

			this.widget = new Slider(0, 0, INPUT_FIELD_WIDTH, ConfigListWidget.this.defaultEntryHeight, Component.nullToEmpty(String.valueOf(value)), min, max, (double) (this.value - min) / (max - min));
			this.widget.setTooltip(this.tooltip);
		}

		@Override
		void updateWidgetText() {
			((Slider) this.widget).setValue((double) (this.value - this.min) / (this.max - this.min));
		}

		public class Slider extends AbstractSliderButton {
			private final double min;
			private final double max;

			public Slider(int x, int y, int width, int height, Component message, double min, double max, double value) {
				super(x, y, width, height, message, value);
				this.min = min;
				this.max = max;
			}

			@Override
			protected void updateMessage() {
				this.setMessage(Component.nullToEmpty(String.valueOf((int) this.getValue())));
			}

			@Override
			protected void applyValue() {
				IntSliderConfigEntry.this.value = (int) this.getValue();
				ConfigListWidget.this.update();
			}

			public double getValue() {
				return this.value * (max - min) + min;
			}

			public void setValue(double value) {
				this.value = value;
				updateMessage();
				applyValue();
			}
		}
	}

	class TextFieldEntry extends ConfigEntry<String> {
		CharPredicate filter;

		TextFieldEntry(String label, String value, String defaultValue, CharPredicate filter, Consumer<String> saveCallback) {
			super(label, value, defaultValue, saveCallback);
			this.filter = filter;

			var textWidget = new EditBox(ConfigListWidget.this.minecraft.font, 0, 0, INPUT_FIELD_WIDTH, 20, Component.empty()) {
				@Override
				public boolean charTyped(CharacterEvent input) {
					if (TextFieldEntry.this.filter.test((char) input.codepoint())) {
						return super.charTyped(input);
					}
					return false;
				}
			};

			this.widget = textWidget;
			textWidget.setMaxLength(Integer.MAX_VALUE);
			textWidget.setValue(String.valueOf(this.value));
			textWidget.setResponder(text -> {
				this.value = text;
				ConfigListWidget.this.update();
			});

			textWidget.setTooltip(this.tooltip);
		}

		@Override
		void updateWidgetText() {
			((EditBox) this.widget).setValue(this.value);
		}
	}

	class IntFieldEntry extends TextFieldEntry {
		IntFieldEntry(String label, int value, int defaultValue, IntConsumer saveCallback) {
			super(label, String.valueOf(value), String.valueOf(defaultValue), chr -> (chr >= '0' && chr <= '9') || chr == '-', str -> saveCallback.accept(parseInt(str)));

			((EditBox) this.widget).setResponder(text -> {
				this.value = text.isEmpty() ? "0" : text;
				ConfigListWidget.this.update();
			});
		}

		private static int parseInt(String str) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				boolean negative = !str.isEmpty() && str.charAt(0) == '-';
				return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
			}
		}

		@Override
		void updateWidgetText() {
			((EditBox) this.widget).setValue(String.valueOf(this.value));
		}
	}
}
