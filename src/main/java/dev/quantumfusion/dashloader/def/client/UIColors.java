package dev.quantumfusion.dashloader.def.client;

import java.awt.Color;

public class UIColors {
	public static final Color RED_COLOR = new Color(0xff6188);
	public static final Color ORANGE_COLOR = new Color(0xfc9867);
	public static final Color YELLOW_COLOR = new Color(0xffd866);
	public static final Color GREEN_COLOR = new Color(0xa9dc76);
	public static final Color BLUE_COLOR = new Color(0x78DCE8);
	public static final Color PURPLE_COLOR = new Color(0xAB9DF2);

	public static final Color TEXT_COLOR = new Color(0xfcfcfa);
	public static final Color BASE_0 = new Color(0x19181a);
	public static final Color BASE_1 = new Color(0x221f22);
	public static final Color BASE_2 = new Color(0x2d2a2e);

	public static final Color[] LINE_COLORS = {RED_COLOR, ORANGE_COLOR, YELLOW_COLOR, GREEN_COLOR, BLUE_COLOR, PURPLE_COLOR};




	public static Color getProgressColor(double progress) {
		return mix(progress, RED_COLOR, ORANGE_COLOR, YELLOW_COLOR, GREEN_COLOR);
	}

	public static Color mix(double pos, Color... colors) {
		pos = Math.min(1, pos);
		int breaks = colors.length - 1;
		if (pos == 1) return colors[breaks];
		int colorPos = (int) Math.floor(pos * (breaks));
		final double step = 1d / (breaks);
		double localRatio = (pos % step) * breaks;
		return blend(colors[colorPos], colors[colorPos + 1], localRatio);
	}

	public static Color blend(Color i1, Color i2, double ratio) {
		if (ratio > 1f) ratio = 1f;
		else if (ratio < 0f) ratio = 0f;
		double iRatio = 1.0f - ratio;

		int a = (int) ((i1.getAlpha() * iRatio) + (i2.getAlpha() * ratio));
		int r = (int) ((i1.getRed() * iRatio) + (i2.getRed() * ratio));
		int g = (int) ((i1.getGreen() * iRatio) + (i2.getGreen() * ratio));
		int b = (int) ((i1.getBlue() * iRatio) + (i2.getBlue() * ratio));

		return new Color(r, g, b, a);
	}

	public static void print(int color) {
		int r2 = ((color & 0xff0000) >> 16);
		int g2 = ((color & 0xff00) >> 8);
		int b2 = (color & 0xff);
		System.out.println("\033[48;2;" + r2 + ";" + g2 + ";" + b2 + "m");
	}
}
