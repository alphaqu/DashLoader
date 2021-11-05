package dev.quantumfusion.dashloader.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.ConfigHandler;
import dev.quantumfusion.dashloader.def.api.option.data.DashConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static dev.quantumfusion.dashloader.core.ui.DashLoaderProgress.PROGRESS;
import static dev.quantumfusion.dashloader.def.client.UIColors.*;
import static dev.quantumfusion.dashloader.def.client.UIDrawer.TextOrientation.TEXT_LEFT;

public class DashCachingScreen extends Screen {
	public static final List<String> SUPPORTERS = new ArrayList<>();
	public static boolean CACHING_COMPLETE = false;
	private final int barSize = 2;
	private int padding;

	private final Screen previousScreen;

	private final Random random = new Random();
	private final UIDrawer drawer = new UIDrawer();
	private final List<Line> lines = new ArrayList<>();
	private final List<Pair<Color, Integer>> lineColorSelectors = new ArrayList<>();
	private float weight = 0;
	private boolean debug;
	private int frame = 0;

	private final String fact = HahaManager.getFact();

	private double currentProgress = 0;
	private long oldTime = System.currentTimeMillis();

	public DashCachingScreen(Screen previousScreen) {
		super(Text.of("Caching"));
		UIColors.loadConfig(ConfigHandler.CONFIG);
		this.previousScreen = previousScreen;
		this.padding = ConfigHandler.CONFIG.cacheScreenPaddingSize;
		this.debug = ConfigHandler.CONFIG.debug;
		drawer.update(MinecraftClient.getInstance(), this::fillGradient);
		createLines();
	}

	private void createLines() {
		weight = 0;
		lineColorSelectors.clear();
		lines.clear();
		final DashConfig config = ConfigHandler.CONFIG;

		config.lineColors.forEach((s, integer) -> {
			weight += integer;
			lineColorSelectors.add(Pair.of(UIColors.parseColor(s), integer));
		});

		for (int i = 0; i < config.cacheScreenLines; i++) {
			final int height = random.nextInt(5) + 5;
			final float speed = random.nextFloat() + 1; // 1 to 2
			final float x = drawer.getWidth() * random.nextFloat();
			final float y = drawer.getHeight() * random.nextFloat();
			final Line e = new Line(UIDrawer.GradientOrientation.GRADIENT_LEFT, x, y, 100, height, BACKGROUND_COLOR, speed);
			e.updateColor();
			lines.add(e);
		}
	}

	public Color getLineColor() {
		float target = random.nextFloat() * weight;
		float countWeight = 0.0f;
		for (Pair<Color, Integer> item : lineColorSelectors) {
			countWeight += item.getValue();
			if (countWeight >= target)
				return item.getKey();
		}
		throw new RuntimeException("what");
	}

	static {
		try (var input = URI.create("https://quantumfusion.dev/supporters.txt").toURL().openStream()) {
			final String s = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			SUPPORTERS.addAll(Arrays.asList(s.split("\n")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (!debug) {
			final Thread thread = new Thread(DashLoader.INSTANCE::saveDashCache);
			thread.setName("dld-thread");
			thread.start();
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (CACHING_COMPLETE && !debug) {
			MinecraftClient.getInstance().setScreen(this.previousScreen);
			DashLoader.INSTANCE.reloadComplete();
			CACHING_COMPLETE = false;
		}

		drawer.push(matrices, textRenderer);
		if (debug) {
			frame++;


			if (frame % 20 == 0) {
				ConfigHandler.updateFile();
				UIColors.loadConfig(ConfigHandler.CONFIG);
				this.padding = ConfigHandler.CONFIG.cacheScreenPaddingSize;
				weight = 0;
				lineColorSelectors.clear();
				final DashConfig config = ConfigHandler.CONFIG;
				config.lineColors.forEach((s, integer) -> {
					weight += integer;
					lineColorSelectors.add(Pair.of(UIColors.parseColor(s), integer));
				});
			}
		}

		final int width = drawer.getWidth();
		final int height = drawer.getHeight();


		drawer.drawQuad(BACKGROUND_COLOR, 0, 0, width, height);
		for (Line line : lines) {
			line.tick();
		}


		drawLines(lines, matrices);


		updateProgress(debug ? (mouseX / (double) width) : PROGRESS.getProgress());

		final int barY = height - padding - barSize;
		drawer.drawQuad(PROGRESS_LANE_COLOR, 0, barY, width, barSize); // progress back
		drawer.drawQuad(getProgressColor(currentProgress), 0, barY, (int) (width * currentProgress), barSize); // the progress bar
		drawer.drawText(TEXT_LEFT,debug ? "Debug mode is activated in DashLoader config." : PROGRESS.getSubtaskName(), TEXT_COLOR, padding, barY - padding); // current task


		// fun fact
		drawer.drawText(TEXT_LEFT, fact, TEXT_COLOR, padding, padding + textRenderer.fontHeight);
		super.render(matrices, mouseX, mouseY, delta);
	}

	private static double calcDelta(double targetProgress, double currentProgress, double timeOff) {
		double delta = targetProgress - currentProgress;
		return delta == 0 ? 0 : (delta / (delta < 0 ? 10 : 20)) / timeOff;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		super.resize(client, width, height);
		drawer.update(client, this::fillGradient);
	}

	private void updateProgress(double targetProgress) {
		long currentTime = System.currentTimeMillis();
		final long deltaTime = currentTime - oldTime;
		if (deltaTime > 16) {
			this.currentProgress += calcDelta(targetProgress, currentProgress, deltaTime / 16d);
			this.oldTime = currentTime;
		}
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
			final Color color = line.color;
			Color end = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
			fillGradient(line.orientation, ms.peek().getModel(), bufferBuilder, (int) line.x, (int) line.y, (int) line.x + line.width, (int) line.y + line.height, color, end);
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
		public final UIDrawer.GradientOrientation orientation;
		public final int width;
		public final int height;
		public final float speed;
		public Color color;
		public float x;
		public float y;

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
			this.x += (orientation.xDir * (speed * 4f)) / 2f;
			this.y += (orientation.yDir * (speed * 4f)) / 2f;

			if (x - width > drawer.getWidth()) {
				this.x = 0;
				this.y = random.nextInt(drawer.getHeight());
				updateColor();
			} else if (x + width < 0) {
				this.x = drawer.getWidth();
				this.y = random.nextInt(drawer.getHeight());
				updateColor();
			} else if (y - height > drawer.getHeight()) {
				this.y = 0;
				this.x = random.nextInt(drawer.getWidth());
				updateColor();
			} else if (y + height < 0) {
				this.y = drawer.getHeight();
				this.x = random.nextInt(drawer.getWidth());
				updateColor();
			}
		}

		private void updateColor() {
			this.color = getLineColor();
		}
	}

}
