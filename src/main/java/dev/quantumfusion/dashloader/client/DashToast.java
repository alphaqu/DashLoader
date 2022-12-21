package dev.quantumfusion.dashloader.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.quantumfusion.dashloader.ProgressHandler;
import dev.quantumfusion.taski.builtin.StaticTask;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dev.quantumfusion.dashloader.DashLoader.DL;

public class DashToast implements Toast {
	private static final int PROGRESS_BAR_HEIGHT = 2;
	private static final int PADDING = 8;
	private static final int LINES = 200;
	public static Status STATUS = Status.IDLE;
	private final Random random = new Random();
	private final List<Line> lines = new ArrayList<>();
	private final String fact = HahaManager.getFact();

	private final long start = System.currentTimeMillis();
	private long oldTime = System.currentTimeMillis();
	private boolean done = false;

	public int getWidth() {
		return 200;
	}

	public int getHeight() {
		return 40;
	}

	public DashToast() {
		DashToast.STATUS = DashToast.Status.CACHING;
		final Thread thread = new Thread(DL::saveDashCache);
		thread.setName("dashloader-thread");
		thread.start();

		// Create lines
		for (int i = 0; i < LINES; i++) {
			this.lines.add(new Line());
		}
	}


	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
		TextRenderer textRenderer = manager.getClient().textRenderer;

		if ((STATUS == Status.DONE) && !done) {
			DL.resetDashLoader();
			done = true;
		}

		final int width = this.getWidth();
		final int height = this.getHeight();
		final int barY = height - PROGRESS_BAR_HEIGHT;

		// Get progress
		double currentProgress;
		Color currentProgressColor;
		if (STATUS == Status.CRASHED) {
			ProgressHandler.TASK = new StaticTask("Crash", (System.currentTimeMillis() - start) / (float) 10000);
			DL.progress.setCurrentTask("Internal crash. Please check logs.");
			currentProgress = DL.progress.getProgress();
			currentProgressColor = DrawerUtil.FAILED_COLOR;
		} else {
			currentProgress = DL.progress.getProgress();
			currentProgressColor = DrawerUtil.getProgressColor(currentProgress);
		}

		// Tick progress
		while (this.oldTime < System.currentTimeMillis()) {
			for (Line line : this.lines) {
				line.tick(width, height, (float) currentProgress);
			}
			// about 60fps
			this.oldTime += 17;
		}
		currentProgress = MathHelper.clamp(currentProgress, 0.0, 1.0);

		// Setup scissor
		{
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			Vector4f vec = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
			vec.mul(matrixStack.peek().getPositionMatrix());
			Window window = manager.getClient().getWindow();
			double scale = window.getScaleFactor();
			RenderSystem.enableScissor(
					(int) (vec.x * scale),
					(int) (window.getFramebufferHeight() - (vec.y * scale) - this.getHeight() * scale),
					(int) (this.getWidth() * scale),
					(int) (this.getHeight() * scale));
		}

		// Draw the ui
		DrawerUtil.drawRect(matrices, -100, -100, 100, 100, Color.red);

		DrawerUtil.drawRect(matrices, 0, 0, width, height, DrawerUtil.BACKGROUND_COLOR);
		this.drawLines(currentProgress, matrices);
		DrawerUtil.drawRect(matrices, 0, barY, width, PROGRESS_BAR_HEIGHT, DrawerUtil.PROGRESS_TRACK);
		DrawerUtil.drawRect(matrices, 0, barY, (int) (width * currentProgress), PROGRESS_BAR_HEIGHT, currentProgressColor);
		DrawerUtil.drawText(matrices, textRenderer, PADDING, barY - PADDING, DL.progress.getCurrentTask(), DrawerUtil.STATUS_COLOR);
		DrawerUtil.drawText(matrices, textRenderer, PADDING, textRenderer.fontHeight + PADDING, this.fact, DrawerUtil.FOREGROUND_COLOR);

		RenderSystem.disableScissor();

		if (STATUS == Status.CRASHED && System.currentTimeMillis() - start > 10000) {
			return Visibility.HIDE;
		}

		if ((done && System.currentTimeMillis() - start > 2000)) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}

	private void drawLines(double progress, MatrixStack ms) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Matrix4f matrix = ms.peek().getPositionMatrix();
		for (Line line : lines) {
			line.draw(matrix, bufferBuilder, progress);
		}

		tessellator.draw();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}


	private final class Line {

		private static final float SPEED = 1.0f;
		public ColorKind colorKind;
		public float x;
		public float y;
		public int width;
		public int height;
		// The speed modifier
		public float speedBoost;

		public Line() {
			this.x = -1000;
			this.y = -1000;
			this.width = DashToast.this.random.nextInt(30, 50);
			this.height = DashToast.this.random.nextInt(2, 5);
			this.colorKind = ColorKind.Neutral;
		}

		public void tick(int screenWidth, int screenHeight, float progress) {
			// Move the values
			float progressSpeed = (float) (0.5 + (2.0) * progress);
			this.x += SPEED * (speedBoost + 1.0) * progressSpeed;


			// Check if out of bounds
			if (this.x > screenWidth || this.x + this.width < 0) {
				// Randomize vertical position
				this.x = -this.width;
				this.y = screenHeight * DashToast.this.random.nextFloat();

				// Randomise speed
				this.speedBoost = DashToast.this.random.nextFloat() * 2f;

				// Randomise color
				if (DashToast.this.random.nextFloat() > 0.95) {
					this.colorKind = ColorKind.Progress;
				} else {
					this.colorKind = ColorKind.Neutral;
				}
			}
		}

		public void draw(Matrix4f b4, BufferBuilder bb, double progress) {
			// Get colors
			Color color = getColor(progress);
			Color end = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

			if (STATUS == Status.CRASHED) {
				color = DrawerUtil.FAILED_COLOR;
			}

			drawVertex(b4, bb, x + width, y, color); // right top
			drawVertex(b4, bb, x, y, end); // left top
			drawVertex(b4, bb, x, y + height, end); // left bottom
			drawVertex(b4, bb, x + width, y + height, color); // right bottom
		}

		public Color getColor(double progress) {
			Color color = switch (this.colorKind) {
				case Neutral -> DrawerUtil.NEUTRAL_LINE;
				case Progress -> DrawerUtil.getProgressColor(progress);
			};
			float opacity = MathHelper.clamp(((this.x) / (float) this.width), 0.0f, 1.0f);
			return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
		}

		private static void drawVertex(Matrix4f m4f, BufferBuilder bb, float x, float y, Color color) {
			bb.vertex(m4f, x, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
		}
	}

	public enum Status {
		IDLE,
		CACHING,
		CRASHED,
		DONE
	}

	public enum ColorKind {
		Neutral,
		Progress
	}
}
