package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.ObjectObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.model.components.BakedQuadCollection;
import dev.notalpha.dashloader.client.model.components.DashBakedQuad;
import dev.notalpha.dashloader.client.model.components.DashBakedQuadCollection;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.Direction;

/**
 * Cached form of a {@link BlockModelPart} (1.21.5+ model system).
 * Parts are collected from {@link net.minecraft.client.renderer.block.model.BlockStateModel#collectParts} on SAVE
 * and reimplemented directly on LOAD, no vanilla bake needed.
 */
public final class DashBlockModelPart implements DashObject<BlockModelPart, DashBlockModelPart.DazyImpl> {
	public final int quads;
	public final ObjectObjectList<Direction, Integer> faceQuads;
	public final boolean useAo;
	public final int sprite;

	public DashBlockModelPart(int quads,
	                          ObjectObjectList<Direction, Integer> faceQuads,
	                          boolean useAo,
	                          int sprite) {
		this.quads = quads;
		this.faceQuads = faceQuads;
		this.useAo = useAo;
		this.sprite = sprite;
	}

	public DashBlockModelPart(BlockModelPart part, RegistryWriter writer) {
		this.quads = writer.add(new BakedQuadCollection(part.getQuads(null)));
		this.faceQuads = new ObjectObjectList<>();
		for (Direction direction : Direction.values()) {
			List<BakedQuad> forDirection = part.getQuads(direction);
			if (forDirection.isEmpty()) {
				continue;
			}
			this.faceQuads.put(direction, writer.add(new BakedQuadCollection(forDirection)));
		}
		this.useAo = part.useAmbientOcclusion();
		this.sprite = writer.add(part.particleIcon());
	}

	@Override
	public DazyImpl export(RegistryReader reader) {
		DashBakedQuadCollection.DazyImpl quadsOut = reader.get(this.quads);
		Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuadsOut = new HashMap<>();
		for (var entry : this.faceQuads.list()) {
			faceQuadsOut.put(entry.key(), reader.get(entry.value()));
		}
		return new DazyImpl(quadsOut, faceQuadsOut, this.useAo, reader.get(this.sprite));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBlockModelPart that = (DashBlockModelPart) o;

		if (useAo != that.useAo) return false;
		if (quads != that.quads) return false;
		if (sprite != that.sprite) return false;
		return faceQuads.equals(that.faceQuads);
	}

	@Override
	public int hashCode() {
		int result = quads;
		result = 31 * result + faceQuads.hashCode();
		result = 31 * result + (useAo ? 1 : 0);
		result = 31 * result + sprite;
		return result;
	}

	public static class DazyImpl extends Dazy<BlockModelPart> {
		public final DashBakedQuadCollection.DazyImpl quads;
		public final Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuads;
		public final boolean useAo;
		public final DashSprite.DazyImpl sprite;

		public DazyImpl(DashBakedQuadCollection.DazyImpl quads,
		                Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuads,
		                boolean useAo,
		                DashSprite.DazyImpl sprite) {
			this.quads = quads;
			this.faceQuads = faceQuads;
			this.useAo = useAo;
			this.sprite = sprite;
		}

		@Override
		protected BlockModelPart resolve(SpriteGetter spriteLoader) {
			List<BakedQuad> quadsOut = this.quads.get(spriteLoader);
			Map<Direction, List<BakedQuad>> faceQuadsOut = new HashMap<>();
			this.faceQuads.forEach((direction, dazy) -> faceQuadsOut.put(direction, dazy.get(spriteLoader)));
			return new Impl(quadsOut, faceQuadsOut, this.useAo, this.sprite.get(spriteLoader));
		}

		/** Direct {@link BlockModelPart} implementation backed by cached data. */
		public static final class Impl implements BlockModelPart {
			private final List<BakedQuad> quads;
			private final Map<Direction, List<BakedQuad>> faceQuads;
			private final boolean useAo;
			private final TextureAtlasSprite sprite;

			public Impl(List<BakedQuad> quads, Map<Direction, List<BakedQuad>> faceQuads, boolean useAo, TextureAtlasSprite sprite) {
				this.quads = quads;
				this.faceQuads = faceQuads;
				this.useAo = useAo;
				this.sprite = sprite;
			}

			@Override
			public List<BakedQuad> getQuads(@Nullable Direction side) {
				if (side == null) {
					return this.quads;
				}
				return this.faceQuads.getOrDefault(side, List.of());
			}

			@Override
			public boolean useAmbientOcclusion() {
				return this.useAo;
			}

			@Override
			public TextureAtlasSprite particleIcon() {
				return this.sprite;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Impl impl = (Impl) o;
				return useAo == impl.useAo && Objects.equals(quads, impl.quads) && Objects.equals(faceQuads, impl.faceQuads) && Objects.equals(sprite, impl.sprite);
			}

			@Override
			public int hashCode() {
				return Objects.hash(quads, faceQuads, useAo, sprite);
			}
		}
	}
}

