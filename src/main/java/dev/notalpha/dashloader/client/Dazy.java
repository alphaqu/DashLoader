package dev.notalpha.dashloader.client;

import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.jetbrains.annotations.Nullable;

// its lazy, but dash! Used for resolution of sprites.
public abstract class Dazy<V> {
	@Nullable
	private transient volatile V loaded;
	@Nullable
	private transient volatile SpriteGetter loader;

	protected abstract V resolve(SpriteGetter spriteLoader);

	// The same instance is reachable from the bake continuation on the reload
	// executor and from the render thread, and instances are reused across
	// reloads. Without synchronization both sides could pass the null check and
	// build two copies of the same object graph, with the loser's copy never
	// published but still handed to its caller.
	public V get(SpriteGetter spriteLoader) {
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


