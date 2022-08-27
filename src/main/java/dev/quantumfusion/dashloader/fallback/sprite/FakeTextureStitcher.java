package dev.quantumfusion.dashloader.fallback.sprite;

import java.util.Map;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.MutablePair;

public class FakeTextureStitcher extends TextureStitcher {
	private final int width;
	private final int height;
	private final Map<Identifier, MutablePair<Sprite, Sprite.Info>> cachedSprites;

	public FakeTextureStitcher(int width, int height, int mipLevel, Map<Identifier, MutablePair<Sprite, Sprite.Info>> cachedSprites) {
		super(width, height, mipLevel);
		this.width = width;
		this.height = height;
		this.cachedSprites = cachedSprites;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void add(Sprite.Info info) {
	}

	@Override
	public void stitch() {
	}

	@Override
	public void getStitchedSprites(SpriteConsumer consumer) {
		this.cachedSprites.forEach((identifier, entry) -> {
			Sprite.Info info = entry.getRight();
			Sprite sprite = entry.getLeft();
			if (info == null) {
				if (MissingSprite.getMissingSpriteId().equals(identifier)) {
					info = MissingSprite.getMissingInfo();
				}
			}
			consumer.load(info, sprite.getWidth(), sprite.getHeight(), sprite.getX(), sprite.getY());
		});
	}
}
