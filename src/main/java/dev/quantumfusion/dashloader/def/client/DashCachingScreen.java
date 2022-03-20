package dev.quantumfusion.dashloader.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.core.config.DashConfig;
import dev.quantumfusion.dashloader.core.progress.task.CountTask;
import dev.quantumfusion.dashloader.core.progress.task.DummyTask;
import dev.quantumfusion.dashloader.core.progress.task.Task;
import dev.quantumfusion.dashloader.def.DashLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.quantumfusion.dashloader.def.client.UIColors.*;
import static dev.quantumfusion.dashloader.def.client.UIDrawer.TextOrientation.TEXT_LEFT;

public class DashCachingScreen extends Screen {
	public static final List<String> SUPPORTERS = new ArrayList<>();
	public static Status STATUS = Status.IDLE;
	private static Color FAILED_COLOR = new Color(255, 75, 69);

	private final Screen previousScreen;

	private final Random random = new Random();
	private final UIDrawer drawer = new UIDrawer();
	private final List<Line> lines = new ArrayList<>();
	private final List<Pair<Color, Integer>> lineColorSelectors = new ArrayList<>();
	private float weight = 0;
	private boolean debug;
	private int padding;
	private int progressBarHeight = 0;
	private float lineSpeedDifference = 0;

	private boolean configRequiresUpdate = false;
	private final String fact = HahaManager.getFact();

	private long oldTime = System.currentTimeMillis();

	public DashCachingScreen(Screen previousScreen) {
		super(Text.of("Caching"));
		UIColors.loadConfig(DashLoaderCore.CONFIG.config);
		this.previousScreen = previousScreen;
		drawer.update(MinecraftClient.getInstance(), this::fillGradient);
		updateConfig();
	}

	private void updateConfig() {
		final DashConfig config = DashLoaderCore.CONFIG.config;
		UIColors.loadConfig(config);

		this.padding = config.paddingSize;
		this.debug = config.debugMode;
		this.progressBarHeight = config.progressBarHeight;
		this.lineSpeedDifference = config.lineSpeedDifference;
		int lineAmount = config.lineAmount;

		// lines
		weight = 0;
		lineColorSelectors.clear();
		config.lineColors.forEach((s, integer) -> {
			weight += integer;
			lineColorSelectors.add(Pair.of(UIColors.parseColor(s), integer));
		});

		UIDrawer.GradientOrientation lineOrientation = UIDrawer.GradientOrientation.GRADIENT_LEFT;

		final String lineDirection = config.lineDirection;
		switch (lineDirection) {
			case "UP" -> lineOrientation = UIDrawer.GradientOrientation.GRADIENT_TOP;
			case "LEFT" -> lineOrientation = UIDrawer.GradientOrientation.GRADIENT_LEFT;
			case "RIGHT" -> lineOrientation = UIDrawer.GradientOrientation.GRADIENT_RIGHT;
			case "DOWN" -> lineOrientation = UIDrawer.GradientOrientation.GRADIENT_DOWN;
			default -> DashLoader.LOGGER.error("Direction {} does not exist. (LEFT, RIGHT, UP, DOWN)", lineDirection);
		}

		lineSpeedDifference = config.lineSpeedDifference;

		if (lineAmount > lines.size()) {
			for (int i = 0; i < (lineAmount - lines.size()); i++) {
				lines.add(new Line());
			}
		} else if (lineAmount < lines.size()) {
			final int toRemove = lines.size() - lineAmount;
			if (toRemove > 0) lines.subList(0, toRemove).clear();
		}

		for (Line line : lines) {
			line.speed = config.lineSpeed;
			line.width = config.lineWidth;
			final int bound = config.lineMaxHeight - config.lineMinHeight;
			line.height = config.lineMinHeight + (bound > 0 ? random.nextInt(bound) : 0);
			line.orientation = lineOrientation;
			line.updateStep();
		}


		if (this.debug) {
			DashLoaderCore.PROGRESS.setCurrentTask("debug");
			DashLoaderCore.CONFIG.addListener(listener -> configRequiresUpdate = true);
		}

	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	public Color getLineColor() {
		float target = random.nextFloat() * weight;
		float countWeight = 0.0f;
		for (Pair<Color, Integer> item : lineColorSelectors) {
			countWeight += item.getValue();
			if (countWeight >= target)
				return item.getKey();
		}
		throw new RuntimeException("Could not get line color.");
	}

	static {
		try (var input = URI.create("https://quantumfusion.dev/supporters.txt").toURL().openStream()) {
			final String s = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			SUPPORTERS.addAll(Arrays.asList(s.split("\n")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void init() {
		if (!debug) {
			final Thread thread = new Thread(DashLoader.INSTANCE::saveDashCache);
			thread.setName("dld-thread");
			thread.start();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER && STATUS == Status.CRASHED) {
			STATUS = Status.DONE;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if ((STATUS == Status.DONE) && !debug) {
			MinecraftClient.getInstance().setScreen(this.previousScreen);
			DashLoader.INSTANCE.resetDashLoader();
			STATUS = Status.IDLE;
		}

		if (configRequiresUpdate) {
			updateConfig();
			configRequiresUpdate = false;
		}


		drawer.push(matrices, textRenderer);
		final int width = drawer.getWidth();
		final int height = drawer.getHeight();
		final int barY = height - padding - progressBarHeight;

		double currentProgress;
		Color currentProgressColor;
		if (debug) {
			currentProgress = Math.max(Math.min(1, mouseX / (double) width), 0);
			currentProgressColor = getProgressColor(currentProgress);
		} else if (STATUS == Status.CRASHED) {
			DashLoaderCore.PROGRESS.setTask(DummyTask.FULL);
			DashLoaderCore.PROGRESS.setCurrentTask("Internal crash. Please check logs or press ENTER.");
			currentProgress = DashLoaderCore.PROGRESS.getProgress();
			currentProgressColor = FAILED_COLOR;
		} else {
			currentProgress = DashLoaderCore.PROGRESS.getProgress();
			currentProgressColor = getProgressColor(currentProgress);
		}

		drawer.drawQuad(BACKGROUND_COLOR, 0, 0, width, height);

		while (oldTime < System.currentTimeMillis()) {
			for (Line line : lines) line.tick();
			// about 60fps
			oldTime += 17;
		}

		drawLines(lines, matrices);
		drawer.drawQuad(PROGRESS_LANE_COLOR, 0, barY, width, progressBarHeight); // progress back
		drawer.drawQuad(currentProgressColor, 0, barY, (int) (width * currentProgress), progressBarHeight); // the progress bar
		drawer.drawText(TEXT_LEFT, DashLoaderCore.PROGRESS.getCurrentTask(), TEXT_COLOR, padding, barY - padding); // current task

		// fun fact
		drawer.drawText(TEXT_LEFT, fact, TEXT_COLOR, padding, padding + textRenderer.fontHeight);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		super.resize(client, width, height);
		drawer.update(client, this::fillGradient);
	}


	private void drawLines(List<Line> lines, MatrixStack ms) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		for (Line line : lines) {
			Color color = line.color;
			Color end = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
			if (STATUS == Status.CRASHED) {
				color = FAILED_COLOR.darker().darker();
			}
			fillGradient(line.orientation, ms.peek().getPositionMatrix(), bufferBuilder, (int) line.x, (int) line.y, (int) line.x + line.width, (int) line.y + line.height, color, end);
		}

		tessellator.draw();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	private void fillGradient(UIDrawer.GradientOrientation orientation, Matrix4f b4, BufferBuilder bb, int startX, int startY, int endX, int endY, Color colorStart, Color colorEnd) {
		final int ordinal = orientation.ordinal();
		drawVertex(b4, bb, endX, startY, ordinal == 0 || ordinal == 3 ? colorStart : colorEnd); // right top
		drawVertex(b4, bb, startX, startY, ordinal == 1 || ordinal == 0 ? colorStart : colorEnd); // left top
		drawVertex(b4, bb, startX, endY, ordinal == 2 || ordinal == 1 ? colorStart : colorEnd); // left bottom
		drawVertex(b4, bb, endX, endY, ordinal == 3 || ordinal == 4 ? colorStart : colorEnd); // right bottom
	}

	private static void drawVertex(Matrix4f m4f, BufferBuilder bb, int x, int y, Color color) {
		bb.vertex(m4f, (float) x, (float) y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
	}

	private final class Line {
		public UIDrawer.GradientOrientation orientation;
		public Color color;
		public float x;
		public float y;
		public int width;
		public int height;
		public float speed;

		public float xStep;
		public float yStep;

		public Line() {
			this(UIDrawer.GradientOrientation.GRADIENT_LEFT, -100, -100, 0, 0, Color.WHITE, 1);
		}

		private Line(UIDrawer.GradientOrientation orientation, float x, float y, int width, int height, Color color, float speed) {
			this.orientation = orientation;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.color = color;
			this.speed = speed;
		}

		public void tick() {
			this.x += xStep;
			this.y += yStep;

			final int screenHeight = drawer.getHeight();
			final int screenWidth = drawer.getWidth();
			if (x - this.width > screenWidth) {
				this.x = 0;
				this.y = random.nextInt(screenHeight);
				update();
			} else if (x + this.width < 0) {
				this.x = screenWidth;
				this.y = random.nextInt(screenHeight);
				update();
			} else if (y - this.height > screenHeight) {
				this.y = -height;
				this.x = random.nextInt(screenWidth);
				update();
			} else if (y + this.height < 0) {
				this.y = screenHeight;
				this.x = random.nextInt(screenWidth);
				update();
			}
		}

		private void update() {
			updateColor();
			updateStep();
		}

		private void updateColor() {
			this.color = getLineColor();
		}

		private void updateStep() {
			this.xStep = (orientation.xDir * (speed * (1 + (random.nextFloat() * lineSpeedDifference)))) / 2f;
			this.yStep = (orientation.yDir * (speed * (1 + (random.nextFloat() * lineSpeedDifference)))) / 2f;
		}
	}

	public enum Status {
		IDLE,
		CACHING,
		CRASHED,
		DONE
	}

}
