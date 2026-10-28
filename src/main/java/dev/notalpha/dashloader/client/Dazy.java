package dev.notalpha.dashloader.client;

import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import org.jetbrains.annotations.Nullable;

// its lazy, but dash! Used for resolution of sprites.
public abstract class Dazy<V> {
	@Nullable
	private transient volatile V loaded;
	@Nullable
	private transient volatile ErrorCollectingSpriteGetter loader;

	protected abstract V resolve(ErrorCollectingSpriteGetter spriteLoader);

	public V get(ErrorCollectingSpriteGetter spriteLoader) {
		V local = this.loaded;
		if (local != null && this.loader == spriteLoader) {
			return local;
		}

		synchronized (this) {
			local = this.loaded;
			if (local == null || this.loader != spriteLoader) {
				local = resolve(spriteLoader);
				this.loader = spriteLoader;
				this.loaded = local;
			}
			return local;
		}
	}
}



