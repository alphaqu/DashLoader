package dev.notalpha.dashloader.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

public class DrawerUtil {
	public static final Color FAILED_COLOR = new Color(255, 75, 69);
	public static final Color BACKGROUND_COLOR = new Color(34, 31, 34);
	public static final Color FOREGROUND_COLOR = new Color(252, 252, 250);
	public static final Color STATUS_COLOR = new Color(180, 180, 180);
	public static final Color NEUTRAL_LINE = new Color(45, 42, 46);
	public static final Color PROGRESS_TRACK = new Color(25, 24, 26);
	private static final Color[] PROGRESS_COLORS = new Color[]{
			new Color(0xff, 0x61, 0x88),
			new Color(0xfc, 0x98, 0x67),
			new Color(0xff, 0xd8, 0x66),
			new Color(0xa9, 0xdc, 0x76)
	};

	public static void drawRect(MatrixStack matrixStack, int x, int y, int width, int height, Color color) {
		final int x2 = width + x;
		final int y2 = height + y;
		DrawableHelper.fill(matrixStack, x, y, x2, y2, convertColor(color));
	}

	public static void drawText(MatrixStack matrixStack, TextRenderer textRenderer, int x, int y, String text, Color color) {
		DrawableHelper.drawTextWithShadow(matrixStack, textRenderer, Text.of(text), x, y - (textRenderer.fontHeight), color.getRGB() | 0xff000000);

	}

	public static int convertColor(Color color) {
		return color.getRGB() | color.getAlpha() << 24;
	}

	public static Color getProgressColor(double progress) {
		return mix(progress, PROGRESS_COLORS);
	}

	private static Color mix(double pos, Color... colors) {
		if (colors.length == 1) {
			return colors[0];
		}
		pos = Math.min(1, Math.max(0, pos));
		int breaks = colors.length - 1;
		if (pos == 1) {
			return colors[breaks];
		}
		int colorPos = (int) Math.floor(pos * (breaks));
		final double step = 1d / (breaks);
		double localRatio = (pos % step) * breaks;
		return blend(colors[colorPos], colors[colorPos + 1], localRatio);
	}

	private static Color blend(Color i1, Color i2, double ratio) {
		if (ratio > 1f) {
			ratio = 1f;
		} else if (ratio < 0f) {
			ratio = 0f;
		}
		double iRatio = 1.0f - ratio;

		int a = (int) ((i1.getAlpha() * iRatio) + (i2.getAlpha() * ratio));
		int r = (int) ((i1.getRed() * iRatio) + (i2.getRed() * ratio));
		int g = (int) ((i1.getGreen() * iRatio) + (i2.getGreen() * ratio));
		int b = (int) ((i1.getBlue() * iRatio) + (i2.getBlue() * ratio));

		return new Color(r, g, b, a);
	}
}
