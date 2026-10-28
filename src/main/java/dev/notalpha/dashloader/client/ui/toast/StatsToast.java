package dev.notalpha.dashloader.client.ui.toast;

import dev.notalpha.dashloader.client.ui.DrawerUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;

public class StatsToast implements Toast {
	private static final long LIFETIME_MS = 6000;
	private static final int PADDING = 4;

	private final String text;
	private final int width;
	private Visibility visibility = Visibility.SHOW;
	private long shownAt = -1;

	public StatsToast(String text) {
		this.text = text;
		this.width = text.length() * 6 + PADDING * 2;
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return 14;
	}

	@Override
	public Visibility getWantedVisibility() {
		return this.visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		if (this.shownAt == -1) {
			this.shownAt = System.currentTimeMillis();
		}
		this.visibility = System.currentTimeMillis() - this.shownAt > LIFETIME_MS
				? Visibility.HIDE
				: Visibility.SHOW;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, Font font, long startTime) {
		final int width = width();
		final int height = height();
		context.enableScissor(0, 0, width, height);
		DrawerUtil.drawRect(context, 0, 0, width, height, DrawerUtil.BACKGROUND_COLOR);
		DrawerUtil.drawText(context, font, PADDING, height - PADDING, this.text,
				DrawerUtil.FOREGROUND_COLOR);
		context.disableScissor();
	}
}