package dev.notalpha.dashloader.client.sprite.content;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.IntIntList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import dev.notalpha.taski.builtin.StepTask;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SpriteContentModule implements DashModule<SpriteContentModule.Data> {
	public final static CachingData<Map<Identifier, SpriteContents>> SOURCE = new CachingData<>();

	@Override
	public void reset(Cache cache) {
		SOURCE.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var spriteData = SOURCE.get(CacheStatus.SAVE);
		assert spriteData != null;

		var map = new IntIntList();
		task.doForEach(spriteData, (identifier, spriteContents) -> {
			if (spriteContents != null) {
				try {
					map.put(writer.add(identifier), writer.add(spriteContents));
				} catch (RuntimeException e) {
					// Modded SpriteContents subclasses (e.g. Fusion, see #85) have no ChunkWriter;
					// skipping is safe: the LOAD path falls back to vanilla loading for missing sprites.
					DashLoader.LOG.warn("Skipping uncacheable sprite {} ({}): {}", identifier, spriteContents.getClass().getName(), e.getMessage());
				}
			}
		});

		return new Data(map);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		Map<Identifier, SpriteContents> spriteData = SOURCE.get(CacheStatus.LOAD);
		assert spriteData != null;

		data.sprites.forEach((key, value) -> {
			Identifier identifier = reader.get(key);
			SpriteContents contents = reader.get(value);
			spriteData.put(identifier, contents);
		});
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_SPRITE_CONTENT);
	}

	/**
	 * Compares two sprite contents by pixel data. Used to tell harmless double
	 * opens (identical reloads) apart from genuine same-id clashes where Mojang
	 * reuses one id for different textures (e.g. wither painting vs. wither
	 * effect icon). Only called for duplicate ids, never on the hot path.
	 */
	public static boolean sameContents(SpriteContents a, SpriteContents b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		try {
			NativeImage imageA = ((SpriteContentsAccessor) a).getImage();
			NativeImage imageB = ((SpriteContentsAccessor) b).getImage();
			if (imageA == null || imageB == null) {
				return imageA == imageB;
			}
			int width = imageA.getWidth();
			int height = imageA.getHeight();
			if (width != imageB.getWidth() || height != imageB.getHeight()) {
				return false;
			}
			// FNV-1a over all pixels; sprites are small and duplicates are rare.
			long hashA = 0xcbf29ce484222325L;
			long hashB = 0xcbf29ce484222325L;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					hashA = (hashA ^ (imageA.getColorArgb(x, y) & 0xFFFFFFFFL)) * 0x100000001b3L;
					hashB = (hashB ^ (imageB.getColorArgb(x, y) & 0xFFFFFFFFL)) * 0x100000001b3L;
				}
			}
			return hashA == hashB;
		} catch (RuntimeException e) {
			// Unreadable image data: play it safe and treat as different so the
			// vanilla mechanism is used.
			return false;
		}
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	public static class Data {
		public final IntIntList sprites;

		public Data(IntIntList sprites) {
			this.sprites = sprites;
		}
	}
}
