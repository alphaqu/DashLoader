package dev.notalpha.dashloader.client.sprite.stitch;

import net.minecraft.client.renderer.texture.Stitcher;

public class DashTextureSlot<T extends Stitcher.Entry> {
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	public transient T contents;

	public DashTextureSlot(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
