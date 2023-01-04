package dev.notalpha.dashloader.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.misc.HahaManager;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import dev.quantumfusion.taski.builtin.StaticTask;
import net.minecraft.client.MinecraftClient;
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
import java.util.function.BiConsumer;

public class DashToast implements Toast {
	private static final int PROGRESS_BAR_HEIGHT = 2;
	private static final int PADDING = 8;
	private static final int LINES = 125;
	private final Random random = new Random();
	private List<Line> lines = new ArrayList<>();
	private final String fact = HahaManager.getFact();
	private Status status;
	private final ProgressManager progress;
	private long timeDone = System.currentTimeMillis();
	private long oldTime = System.currentTimeMillis();
	private static void drawVertex(Matrix4f m4f, BufferBuilder bb, float z, float x, float y, Color color) {
		bb.vertex(m4f, x, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
	}

	public int getWidth() {
		return 200;
	}

	public int getHeight() {
		return 40;
	}

	public DashToast(Cache cacheManager) {
		this.progress = new ProgressManager();
		switch (cacheManager.getStatus()) {
			case SAVE -> {
				this.status = DashToast.Status.CACHING;
				final Thread thread = new Thread(() -> {
					long start = System.currentTimeMillis();
					boolean save = cacheManager.save(stepTask -> this.progress.task = stepTask);
					if (save) {
						this.progress.setOverwriteText("Created cache in " + ProfilerUtil.getTimeStringFromStart(start));
						this.status = Status.DONE;
					} else {
						this.progress.setOverwriteText("Internal error, Please check logs.");
						this.progress.task = new StaticTask("Crash", (System.currentTimeMillis() - timeDone) / (float) 10000);
						this.status = Status.CRASHED;
					}
					cacheManager.setStatus(Cache.Status.IDLE);
					this.timeDone = System.currentTimeMillis();
				});
				thread.setName("dashloader-thread");
				thread.start();
			}
			default -> {
				throw new RuntimeException("hi");
			}
		}

		// Create lines
		for (int i = 0; i < LINES; i++) {
			this.lines.add(new Line());
		}
	}


	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
		final int width = this.getWidth();
		final int height = this.getHeight();
		final int barY = height - PROGRESS_BAR_HEIGHT;

		// Get progress
		final float progress;
		final Color progressColor;
		if (status == Status.CRASHED) {
			progress = (float) this.progress.getProgress();
			progressColor = DrawerUtil.FAILED_COLOR;
		} else {
			progress = (float) this.progress.getProgress();
			progressColor = DrawerUtil.getProgressColor(progress);
		}

		// Tick progress
		List<Line> newList = new ArrayList<>();
		List<Line> newListPrio = new ArrayList<>();
		for (Line line : this.lines) {
			if (line.tick(width, height, progress, 17f / Math.max((System.currentTimeMillis() - this.oldTime), 1f))) {
				newListPrio.add(line);
			} else {
				newList.add(line);
			}
		}
		this.oldTime = System.currentTimeMillis();
		this.lines = newList;
		this.lines.addAll(newListPrio);


		// Setup scissor
		{
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			Vector4f vec = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
			vec.mul(matrixStack.peek().getPositionMatrix());
			Window window = manager.getClient().getWindow();
			double scale = window.getScaleFactor();
			RenderSystem.enableScissor(
					(int) (vec.x * scale),
					(int) (window.getFramebufferHeight() - (vec.y * scale) - height * scale),
					(int) (width * scale),
					(int) (height * scale));
		}

		// Draw the ui
		DrawerUtil.drawRect(matrices, 0, 0, width, height, DrawerUtil.BACKGROUND_COLOR);

		// Draw the background lines.
		this.drawBatched(matrices, (matrix4f, bufferBuilder) -> {
			for (Line line : lines) {
				line.draw(matrix4f, bufferBuilder);
			}
		});


		TextRenderer textRenderer = manager.getClient().textRenderer;
		// Draw progress text
		DrawerUtil.drawText(matrices, textRenderer, PADDING, barY - PADDING, this.progress.getText(), DrawerUtil.STATUS_COLOR);
		String progressText = this.progress.getProgressText();
		DrawerUtil.drawText(matrices, textRenderer, (width - PADDING) - textRenderer.getWidth(progressText), barY - PADDING, progressText, DrawerUtil.STATUS_COLOR);

		// Draw the fun fact
		DrawerUtil.drawText(matrices, textRenderer, PADDING, textRenderer.fontHeight + PADDING, this.fact, DrawerUtil.FOREGROUND_COLOR);

		// Draw progress bar
		DrawerUtil.drawRect(matrices, 0, barY, width, PROGRESS_BAR_HEIGHT, DrawerUtil.PROGRESS_TRACK);
		DrawerUtil.drawRect(matrices, 0, barY, (int) (width * progress), PROGRESS_BAR_HEIGHT, progressColor);

		// Epic rtx graphics. aka i slapped some glow on the things.
		this.drawBatched(matrices, (matrix4f, bb) -> {
			// Line glow
			for (Line line : lines) {
				line.drawGlow(matrix4f, bb);
			}
			// Progress bar glow
			DrawerUtil.drawGlow(matrix4f, bb, 0, barY, (int) (width * progress), PROGRESS_BAR_HEIGHT, 0.75f, progressColor, true, true, true, true);
		});
		RenderSystem.disableScissor();

		if (status == Status.CRASHED && System.currentTimeMillis() - timeDone > 10000) {
			return Visibility.HIDE;
		}

		if (status == Status.DONE && System.currentTimeMillis() - timeDone > 2000) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}

	private void drawBatched(MatrixStack ms, BiConsumer<Matrix4f, BufferBuilder> consumer) {
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Matrix4f matrix = ms.peek().getPositionMatrix();
		consumer.accept(matrix, bufferBuilder);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}


	private final class Line {
		public ColorKind colorKind;
		public float x;
		public float y;
		public int width;
		public int height;
		public float speedBoost;
		private Color color;

		public Line() {
			this.x = -1000;
			this.y = -1000;
			this.width = DashToast.this.random.nextInt(30, 50);
			this.height = DashToast.this.random.nextInt(2, 5);
			this.colorKind = ColorKind.Neutral;
			this.color = Color.red;
		}

		public boolean tick(int screenWidth, int screenHeight, float progress, float delta) {
			// Move the values
			this.x += (float) (speedBoost * (0.8 + (2.5 * progress))) * delta;


			// Check if not visible
			if (x > screenWidth || x + width < 0) {
				// Randomize position
				this.x = -width;
				this.y = screenHeight * DashToast.this.random.nextFloat();

				// Randomise color
				if (status == Status.CRASHED) {
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

		public void draw(Matrix4f b4, BufferBuilder bb) {
			Color end = DrawerUtil.withOpacity(color, 0f);
			drawVertex(b4, bb, 0f, x + width, y, color); // right top
			drawVertex(b4, bb, 0f, x, y, end); // left top
			drawVertex(b4, bb, 0f, x, y + height, end); // left bottom
			drawVertex(b4, bb, 0f, x + width, y + height, color); // right bottom
		}


		public void drawGlow(Matrix4f b4, BufferBuilder bb) {
			if (this.colorKind != ColorKind.Neutral) {
				DrawerUtil.drawGlow(b4, bb, x, y, width, height, (getWeight() + 2.0f) / 3.0f, this.color, false, true, false, true);
			}
		}

		public Color getColor(double progress) {
			Color color = switch (this.colorKind) {
				case Neutral -> DrawerUtil.NEUTRAL_LINE;
				case Progress -> {
					if (status == Status.CRASHED) {
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

	public enum Status {
		CACHING,
		CRASHED,
		DONE
	}

	public enum ColorKind {
		Neutral,
		Progress,
		Crashed,
	}
}
