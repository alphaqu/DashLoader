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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

/**
 * Cached form of a {@link BlockStateModelPart} (1.21.5+ model system).
 * Parts are collected from {@link net.minecraft.client.renderer.block.dispatch.BlockStateModel#collectParts} on SAVE
 * and reimplemented directly on LOAD, no vanilla bake needed.
 */
public final class DashBlockModelPart implements DashObject<BlockStateModelPart, DashBlockModelPart.DazyImpl> {
	public final int quads;
	public final ObjectObjectList<Direction, Integer> faceQuads;
	public final boolean useAo;
	public final int sprite;
	public final boolean forceTranslucent;

	public DashBlockModelPart(int quads,
	                          ObjectObjectList<Direction, Integer> faceQuads,
	                          boolean useAo,
	                          int sprite,
	                          boolean forceTranslucent) {
		this.quads = quads;
		this.faceQuads = faceQuads;
		this.useAo = useAo;
		this.sprite = sprite;
		this.forceTranslucent = forceTranslucent;
	}

	public DashBlockModelPart(BlockStateModelPart part, RegistryWriter writer) {
		// One getQuads(null) plus one per direction used to run seven times per
		// part, and every one of them went through writer.add. The directional
		// lists are filtered subsets, so they never deduplicate against the full
		// list: every quad was registered twice, and the empty directions still
		// burned a registry id each. Filtering the single full list and skipping
		// empty results halves the adds and drops the empty ones entirely.
		List<BakedQuad> all = part.getQuads(null);
		this.quads = writer.add(new BakedQuadCollection(all));
		this.faceQuads = new ObjectObjectList<>();
		for (Direction direction : Direction.values()) {
			List<BakedQuad> forDirection = part.getQuads(direction);
			if (forDirection.isEmpty()) {
				continue;
			}
			this.faceQuads.put(direction, writer.add(new BakedQuadCollection(forDirection)));
		}
		this.useAo = part.useAmbientOcclusion();
		Material.Baked particleMaterial = part.particleMaterial();
		this.sprite = writer.add(particleMaterial.sprite());
		this.forceTranslucent = particleMaterial.forceTranslucent();
	}

	@Override
	public DazyImpl export(RegistryReader reader) {
		DashBakedQuadCollection.DazyImpl quadsOut = reader.get(this.quads);
		Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuadsOut = new HashMap<>();
		for (var entry : this.faceQuads.list()) {
			faceQuadsOut.put(entry.key(), reader.get(entry.value()));
		}
		return new DazyImpl(quadsOut, faceQuadsOut, this.useAo, reader.get(this.sprite), this.forceTranslucent);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBlockModelPart that = (DashBlockModelPart) o;

		if (useAo != that.useAo) return false;
		if (quads != that.quads) return false;
		if (sprite != that.sprite) return false;
		if (forceTranslucent != that.forceTranslucent) return false;
		return faceQuads.equals(that.faceQuads);
	}

	@Override
	public int hashCode() {
		int result = quads;
		result = 31 * result + faceQuads.hashCode();
		result = 31 * result + (useAo ? 1 : 0);
		result = 31 * result + sprite;
		result = 31 * result + (forceTranslucent ? 1 : 0);
		return result;
	}

	public static class DazyImpl extends Dazy<BlockStateModelPart> {
		public final DashBakedQuadCollection.DazyImpl quads;
		public final Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuads;
		public final boolean useAo;
		public final DashSprite.DazyImpl sprite;
		public final boolean forceTranslucent;

		public DazyImpl(DashBakedQuadCollection.DazyImpl quads,
		                Map<Direction, DashBakedQuadCollection.DazyImpl> faceQuads,
		                boolean useAo,
		                DashSprite.DazyImpl sprite,
		                boolean forceTranslucent) {
			this.quads = quads;
			this.faceQuads = faceQuads;
			this.useAo = useAo;
			this.sprite = sprite;
			this.forceTranslucent = forceTranslucent;
		}

		@Override
		protected BlockStateModelPart resolve(SpriteGetter spriteLoader) {
			List<BakedQuad> quadsOut = this.quads.get(spriteLoader);
			Map<Direction, List<BakedQuad>> faceQuadsOut = new HashMap<>();
			this.faceQuads.forEach((direction, dazy) -> faceQuadsOut.put(direction, dazy.get(spriteLoader)));
			Material.Baked particleMaterial = new Material.Baked(this.sprite.get(spriteLoader), this.forceTranslucent);
			return new Impl(quadsOut, faceQuadsOut, this.useAo, particleMaterial, materialFlags(quadsOut, faceQuadsOut));
		}

		/** Mirrors {@code QuadCollection} flag computation: OR of every quad's material flags. */
		private static int materialFlags(List<BakedQuad> quads, Map<Direction, List<BakedQuad>> faceQuads) {
			int flags = 0;
			for (BakedQuad quad : quads) {
				flags |= quad.materialInfo().flags();
			}
			for (List<BakedQuad> face : faceQuads.values()) {
				for (BakedQuad quad : face) {
					flags |= quad.materialInfo().flags();
				}
			}
			return flags;
		}

		/** Direct {@link BlockStateModelPart} implementation backed by cached data. */
		public static final class Impl implements BlockStateModelPart {
			private final List<BakedQuad> quads;
			private final Map<Direction, List<BakedQuad>> faceQuads;
			private final boolean useAo;
			private final Material.Baked particleMaterial;
			private final int materialFlags;

			public Impl(List<BakedQuad> quads, Map<Direction, List<BakedQuad>> faceQuads, boolean useAo, Material.Baked particleMaterial, int materialFlags) {
				this.quads = quads;
				this.faceQuads = faceQuads;
				this.useAo = useAo;
				this.particleMaterial = particleMaterial;
				this.materialFlags = materialFlags;
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
			public Material.Baked particleMaterial() {
				return this.particleMaterial;
			}

			@Override
			public int materialFlags() {
				return this.materialFlags;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Impl impl = (Impl) o;
				return useAo == impl.useAo && materialFlags == impl.materialFlags && Objects.equals(quads, impl.quads) && Objects.equals(faceQuads, impl.faceQuads) && Objects.equals(particleMaterial, impl.particleMaterial);
			}

			@Override
			public int hashCode() {
				return Objects.hash(quads, faceQuads, useAo, particleMaterial, materialFlags);
			}
		}
	}
}
