package dev.quantumfusion.dashloader.def.client;

import dev.quantumfusion.dashloader.def.DashLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.quantumfusion.dashloader.core.ui.DashLoaderProgress.PROGRESS;

public class DashCachingScreen extends Screen {
	public static boolean exit = false;
	public static final List<String> SUPPORTERS = new ArrayList<>();
	public static final int BAR_SIZE = 5;
	public static final int PADDING = 4;
	private final Screen previousScreen;
	private double currentProgress = 0;
	private double currentSubProgress = 0;
	private long oldTime = System.currentTimeMillis();

	public DashCachingScreen(Screen previousScreen) {
		super(Text.of("Caching"));
		this.previousScreen = previousScreen;
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
		final Thread thread = new Thread(() -> DashLoader.getInstance().saveDashCache());
		thread.setName("dld-thread");
		thread.start();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		final MinecraftClient instance = MinecraftClient.getInstance();
		final Window window = instance.getWindow();
		final int width = window.getScaledWidth();
		final int height = window.getScaledHeight();

		updateProgress();
		// background
		drawQuad(matrices, UIColors.BACKGROUND_COLOR, 0, 0, width, height);
		// the progress bar
		drawQuad(matrices, UIColors.getProgressColor(currentProgress), 0, (height - (BAR_SIZE * 2)), (int) (width * currentProgress), BAR_SIZE);
		// the bottom progressbar bar
		drawQuad(matrices, UIColors.getProgressColor(currentSubProgress), 0, (height - BAR_SIZE), (int) (width * currentSubProgress), BAR_SIZE);

		// left progress text
		final int textY = (height - (BAR_SIZE * 2) - textRenderer.fontHeight) - (PADDING);
		drawTextWithShadow(matrices, textRenderer, Text.of(PROGRESS.getSubtaskName()), (PADDING * 2), textY, UIColors.TEXT_COLOR);

		// right progress text
		final Text subText = Text.of("haha");
		drawTextWithShadow(matrices, textRenderer, subText, (width - textRenderer.getWidth(subText)), textY, UIColors.TEXT_COLOR);

		int currentSupportersTextY = PADDING * 3;
		drawTextWithShadow(matrices, textRenderer, Text.of("DashLoader was made possible by"), PADDING * 2, currentSupportersTextY, UIColors.TEXT_COLOR);
		currentSupportersTextY += (PADDING) + textRenderer.fontHeight;

		for (int i = 0; i < SUPPORTERS.size(); i++) {
			drawTextWithShadow(matrices, textRenderer, Text.of((i + 1) + ". " + SUPPORTERS.get(i)), (PADDING * 4), currentSupportersTextY, UIColors.TEXT_COLOR);
			currentSupportersTextY += PADDING + textRenderer.fontHeight;
		}

		if (exit) {
			instance.setScreen(this.previousScreen);
			exit = false;
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	private void drawQuad(MatrixStack matrices, int color, int x, int y, int width, int height) {
		fill(matrices, x, y, x + width, y + height, color | 0xff000000);
	}

	private static double calcDelta(double targetProgress, double currentProgress, double timeOff) {
		double delta = targetProgress - currentProgress;
		return delta == 0 ? 0 : (delta / (delta < 0 ? 10 : 20)) / timeOff;
	}

	private void updateProgress() {
		long currentTime = System.currentTimeMillis();
		final long deltaTime = currentTime - oldTime;
		if (deltaTime > 16) {
			this.currentProgress += calcDelta(PROGRESS.getProgress(), currentProgress, deltaTime / 16d);
			this.currentSubProgress += calcDelta(PROGRESS.getSubProgress(), currentSubProgress, deltaTime / 16d);
			this.oldTime = currentTime;
		}
	}

}
