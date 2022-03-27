package dev.quantumfusion.dashloader.def.fallback.sprite;

import dev.quantumfusion.dashloader.def.DashLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureStitcher;

public class FakeTextureStitcher extends TextureStitcher {
	private final int width;
	private final int height;

	public FakeTextureStitcher(int width, int height, int mipLevel) {
		super(width, height, mipLevel);
		this.width = width;
		this.height = height;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void add(Sprite.Info info) {}

	@Override
	public void stitch() {}

	@Override
	public void getStitchedSprites(SpriteConsumer consumer) {
		DashLoader.LOGGER.warn("getStitchedSprites called on stitcher. This is really bad and can cause huge visual problems.");
	}
}
