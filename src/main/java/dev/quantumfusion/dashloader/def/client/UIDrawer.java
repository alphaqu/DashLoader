package dev.quantumfusion.dashloader.def.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.Matrix4f;

import java.awt.Color;

public class UIDrawer {
	private MatrixStack ms;
	private TextRenderer tr;
	private int width;
	private int height;
	private GradientConsumer gradientConsumer;

	public void update(MinecraftClient client, GradientConsumer gradientConsumer) {
		this.gradientConsumer = gradientConsumer;
		final Window window = client.getWindow();
		this.width = window.getScaledWidth();
		this.height = window.getScaledHeight();
	}

	public void push(MatrixStack matrixStack, TextRenderer textRenderer) {
		this.ms = matrixStack;
		this.tr = textRenderer;
	}

	public void drawQuad(Color color, int x, int y, int width, int height) {
		final int x2 = width + x;
		final int y2 = height + y;
		DrawableHelper.fill(ms, x, y, x2, y2, color.getRGB() | 0xff000000);
	}

	public void drawGradient(GradientOrientation orientation, Color colorStart, Color colorEnd, int x, int y, int width, int height) {
		final int x2 = width + x;
		final int y2 = height + y;
		drawInternalGradient(orientation, x, y, x2, y2, colorStart, colorEnd);
	}

	private void drawInternalGradient(GradientOrientation orientation, int startX, int startY, int endX, int endY, Color colorStart, Color colorEnd) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		fillGradient(orientation, ms.peek().getModel(), bufferBuilder, startX, startY, endX, endY, colorStart, colorEnd);
		tessellator.draw();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	private void fillGradient(GradientOrientation orientation, Matrix4f b4, BufferBuilder bb, int startX, int startY, int endX, int endY, Color colorStart, Color colorEnd) {
		final int ordinal = orientation.ordinal();
		drawVertex(b4, bb, endX, startY, ordinal == 0 || ordinal == 3 ? colorStart : colorEnd); // right top
		drawVertex(b4, bb, startX, startY, ordinal == 1 || ordinal == 0 ? colorStart : colorEnd); // left top
		drawVertex(b4, bb, startX, endY, ordinal == 2 || ordinal == 1 ? colorStart : colorEnd); // left bottom
		drawVertex(b4, bb, endX, endY, ordinal == 3 || ordinal == 4 ? colorStart : colorEnd); // right bottom
	}

	private static void drawVertex(Matrix4f m4f, BufferBuilder bb, int x, int y, Color color) {
		bb.vertex(m4f, (float) x, (float) y, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
	}

	public void drawText(TextOrientation orientation, String text, Color color, int x, int y) {
		switch (orientation) {
			case TEXT_LEFT -> drawInternalText(text, color, x, y);
			case TEXT_CENTER -> drawInternalText(text, color, x - (tr.getWidth(text) / 2), y);
			case TEXT_RIGHT -> drawInternalText(text, color, x - tr.getWidth(text), y);
		}
	}


	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	private void drawInternalText(String text, Color color, int xDraw, int yDraw) {
		DrawableHelper.drawTextWithShadow(ms, tr, Text.of(text), xDraw, yDraw - (tr.fontHeight / 2), color.getRGB() | 0xff000000);
	}

	public enum TextOrientation {
		TEXT_LEFT,
		TEXT_CENTER,
		TEXT_RIGHT
	}

	public enum GradientOrientation {
		GRADIENT_TOP(0, -1),
		GRADIENT_LEFT(-1, 0),
		GRADIENT_DOWN(0, 1),
		GRADIENT_RIGHT(1, 0);

		public final int xDir;
		public final int yDir;

		GradientOrientation(int xDir, int yDir) {
			this.xDir = xDir;
			this.yDir = yDir;
		}
	}

	@FunctionalInterface
	public interface GradientConsumer {
		void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd);
	}
}
