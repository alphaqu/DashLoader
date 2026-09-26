package dev.notalpha.dashloader.client.ui.toast;

import dev.notalpha.dashloader.client.ui.Color;
import dev.notalpha.dashloader.client.ui.DrawerUtil;
import dev.notalpha.dashloader.misc.HahaManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DashToast implements Toast {
	private static final int PROGRESS_BAR_HEIGHT = 2;
	private static final int PADDING = 8;
	private static final int LINES = 125;
	public final DashToastState state;
	private final Random random = new Random();
	@Nullable
	private final String fact = HahaManager.getFact();
	private List<Line> lines = new ArrayList<>();
	private long oldTime = System.currentTimeMillis();
	private float progress = 0;
	private Color progressColor = DrawerUtil.getProgressColor(progress);
	private Visibility visibility = Visibility.SHOW;
	// First update() call happens when the toast leaves the ToastManager queue
	// and actually becomes visible. Hide timers run from here, not from the
	// save-thread timestamps: otherwise a toast that waited in the queue
	// (vanilla toasts occupying slots) would hide instantly on arrival.
	private long firstUpdateTime = -1;

	public DashToast() {
		this.state = new DashToastState();
		// Create lines
		for (int i = 0; i < LINES; i++) {
			this.lines.add(new Line());
		}
	}

	public int getWidth() {
		return 200;
	}

	public int getHeight() {
		return 40;
	}

	@Override
	public Visibility getVisibility() {
		return visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		if (firstUpdateTime == -1) {
			firstUpdateTime = System.currentTimeMillis();
		}
		// Get progress
		if (state.getStatus() == DashToastStatus.CRASHED) {
			progress = (float) this.state.getProgress();
			progressColor = DrawerUtil.FAILED_COLOR;
		} else {
			progress = (float) this.state.getProgress();
			progressColor = DrawerUtil.getProgressColor(progress);
		}

		long effectiveDoneTime = Math.max(state.getTimeDone(), firstUpdateTime);
		if (state.getStatus() == DashToastStatus.CRASHED && System.currentTimeMillis() - effectiveDoneTime > 10000) {
			visibility = Visibility.HIDE;
		} else if (state.getStatus() == DashToastStatus.DONE && System.currentTimeMillis() - effectiveDoneTime > 2000) {
			visibility = Visibility.HIDE;
		} else {
			visibility = Visibility.SHOW;
		}
	}

	@Override
	public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
		final int width = this.getWidth();
		final int height = this.getHeight();
		final int barY = height - PROGRESS_BAR_HEIGHT;

		// Tick progress
		List<Line> newList = new ArrayList<>();
		List<Line> newListPrio = new ArrayList<>();
		long currentTime = System.currentTimeMillis();
		for (Line line : this.lines) {
			if (line.tick(width, height, progress, (currentTime - this.oldTime) / 17f)) {
				newListPrio.add(line);
			} else {
				newList.add(line);
			}
		}
		this.oldTime = currentTime;
		this.lines = newList;
		this.lines.addAll(newListPrio);

		// Clip everything to the toast bounds (lines and glow would
		// otherwise spill over the toast edges onto the screen).
		context.enableScissor(0, 0, width, height);

		// Draw the ui
		DrawerUtil.drawRect(context, 0, 0, width, height, DrawerUtil.BACKGROUND_COLOR);

		// Draw the background lines.
		for (Line line : lines) {
			line.draw(context);
		}

		// Draw progress text
		String progressText = this.state.getProgressText();
		int progressTextY = this.fact != null ? barY - PADDING : (barY / 2) + (textRenderer.fontHeight / 2);
		DrawerUtil.drawText(context, textRenderer, PADDING, progressTextY, this.state.getText(), DrawerUtil.STATUS_COLOR);
		DrawerUtil.drawText(context, textRenderer, (width - PADDING) - textRenderer.getWidth(progressText), progressTextY, progressText, DrawerUtil.STATUS_COLOR);

		if (this.fact != null) {
			// Draw the fun fact
			DrawerUtil.drawText(context, textRenderer, PADDING, textRenderer.fontHeight + PADDING, this.fact, DrawerUtil.FOREGROUND_COLOR);
		}

		// Draw progress bar
		DrawerUtil.drawRect(context, 0, barY, width, PROGRESS_BAR_HEIGHT, DrawerUtil.PROGRESS_TRACK);
		DrawerUtil.drawRect(context, 0, barY, (int) (width * progress), PROGRESS_BAR_HEIGHT, progressColor);

		// Epic rtx graphics. aka I slapped some glow on the things.
		// Line glow
		for (Line line : lines) {
			line.drawGlow(context, width, height);
		}
		// Progress bar glow
		DrawerUtil.drawGlowClipped(context, 0, barY, (int) (width * progress), PROGRESS_BAR_HEIGHT, 0.75f, progressColor, true, true, true, true, 0, 0, width, height);

		context.disableScissor();
	}

	public enum ColorKind {
		Neutral,
		Progress,
		Crashed,
	}

	private final class Line {
		public final int width;
		public final int height;
		public ColorKind colorKind;
		public float x;
		public float y;
		public float speedBoost;
		private Color color;

		public Line() {
			this.x = -1000;
			this.y = -1000;
			this.width = DashToast.this.random.nextInt(30, 50);
			this.height = DashToast.this.random.nextInt(2, 5);
			this.colorKind = ColorKind.Neutral;
			this.color = new Color(0xFF0000FF);
		}

		public boolean tick(int screenWidth, int screenHeight, float progress, float delta) {
			// Move the values
			this.x += (float) (speedBoost * (0.8 + (2.5 * progress))) * delta;

			// Check if not visible. Lines are kept inside the toast so no scissor is needed.
			if (x > screenWidth || x + width < 0 || x + width > screenWidth) {
				// Randomize position
				this.x = -width;
				this.y = screenHeight * DashToast.this.random.nextFloat();

				// Randomise color
				if (state.getStatus() == DashToastStatus.CRASHED) {
					if (DashToast.this.random.nextFloat() > 0.9 || this.colorKind == ColorKind.Progress) {
						this.colorKind = ColorKind.Crashed;
					}
				} else {
					if (DashToast.this.random.nextFloat() > 0.95) {
						this.colorKind = ColorKind.Progress;
					} else {
						this.colorKind = ColorKind.Neutral;
					}
				}

				// Randomise speed based on some values.
				// Weight (the size of the line), 0.2 deviation
				float weight = 1f - getWeight();
				float weightSpeed = (float) (0.7 + (weight * 0.6));

				// Kind (The type of line),
				float kindSpeed;
				if (this.colorKind == ColorKind.Neutral) {
					kindSpeed = (float) (1.0 + (DashToast.this.random.nextFloat() * 0.2f));
				} else {
					kindSpeed = (float) (1.0 + (DashToast.this.random.nextFloat() * 0.8f));
				}

				this.speedBoost = kindSpeed * weightSpeed;
				return this.colorKind != ColorKind.Neutral;
			}
			this.color = getColor(progress);

			return false;
		}

		public void draw(DrawContext context) {
			DrawerUtil.drawRect(context, (int) x, (int) y, width, height, color);
		}

		public void drawGlow(DrawContext context, int clipWidth, int clipHeight) {
			if (this.colorKind != ColorKind.Neutral) {
				DrawerUtil.drawGlowClipped(context, x, y, width, height, (getWeight() + 2.0f) / 3.0f, this.color, false, true, false, true, 0, 0, clipWidth, clipHeight);
			}
		}

		public Color getColor(double progress) {
			Color color = switch (this.colorKind) {
				case Neutral -> DrawerUtil.NEUTRAL_LINE;
				case Progress -> {
					if (state.getStatus() == DashToastStatus.CRASHED) {
						yield DrawerUtil.FAILED_COLOR;
					}

					yield DrawerUtil.getProgressColor(progress);
				}
				case Crashed -> DrawerUtil.FAILED_COLOR;
			};

			return DrawerUtil.withOpacity(color, MathHelper.clamp(((this.x) / (this.width)), 0.0f, 1.0f));
		}

		public float getWeight() {
			return ((this.width * (float) this.height) - 60f) / 190f;
		}
	}
}
