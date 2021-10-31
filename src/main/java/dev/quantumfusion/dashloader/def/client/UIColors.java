package dev.quantumfusion.dashloader.def.client;

public class UIColors {
	public static final int RED_COLOR = 0xffff6188;
	public static final int ORANGE_COLOR = 0xfffc9867;
	public static final int YELLOW_COLOR = 0xffffd866;
	public static final int GREEN_COLOR = 0xffa9dc76;

	public static final int TEXT_COLOR = 0xfffcfcfa;
	public static final int BACKGROUND_COLOR = 0xff2d2a2e;


	public static int getProgressColor(double progress) {
		return mix(progress, RED_COLOR, ORANGE_COLOR, YELLOW_COLOR, GREEN_COLOR);
	}

	public static int mix(double pos, int... colors) {
		int breaks = colors.length - 1;
		if(pos == 1) return colors[breaks];
		int colorPos = (int) Math.floor(pos * (breaks));
		final double step = 1d / (breaks);
		double localRatio = (pos % step) * breaks;
		return blend(colors[colorPos], colors[colorPos + 1], localRatio);
	}

	public static int blend(int i1, int i2, double ratio) {
		if (ratio > 1f) ratio = 1f;
		else if (ratio < 0f) ratio = 0f;
		double iRatio = 1.0f - ratio;

		int a1 = (i1 >> 24 & 0xff);
		int r1 = ((i1 & 0xff0000) >> 16);
		int g1 = ((i1 & 0xff00) >> 8);
		int b1 = (i1 & 0xff);

		int a2 = (i2 >> 24 & 0xff);
		int r2 = ((i2 & 0xff0000) >> 16);
		int g2 = ((i2 & 0xff00) >> 8);
		int b2 = (i2 & 0xff);

		int a = (int) ((a1 * iRatio) + (a2 * ratio));
		int r = (int) ((r1 * iRatio) + (r2 * ratio));
		int g = (int) ((g1 * iRatio) + (g2 * ratio));
		int b = (int) ((b1 * iRatio) + (b2 * ratio));

		return a << 24 | r << 16 | g << 8 | b;
	}

	public static void print(int color) {
		int r2 = ((color & 0xff0000) >> 16);
		int g2 = ((color & 0xff00) >> 8);
		int b2 = (color & 0xff);
 		System.out.println("\033[48;2;" + r2 + ";" + g2 + ";" + b2 + "m");
	}
}
