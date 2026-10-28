package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cached form of a {@link BlockStateModel} (1.21.5+ model system).
 * Every model (simple, weighted-resolved, multipart-resolved) is stored as its
 * resolved {@link BlockStateModelPart}s plus particle sprite. Weighted models keep
 * their variants separately in {@link DashWeightedBlockStateModel} so vanilla
 * random picking is preserved.
 */
public final class DashBlockStateModel implements DashObject<BlockStateModel, DashBlockStateModel.DazyImpl> {
	public final List<Integer> parts;
	public final int sprite;
	public final boolean forceTranslucent;

	public DashBlockStateModel(List<Integer> parts, int sprite, boolean forceTranslucent) {
		this.parts = parts;
		this.sprite = sprite;
		this.forceTranslucent = forceTranslucent;
	}

	public DashBlockStateModel(BlockStateModel model, RegistryWriter writer) {
		List<BlockStateModelPart> collected = new ArrayList<>();
		model.collectParts(RandomSource.create(), collected);
		this.parts = new ArrayList<>(collected.size());
		for (BlockStateModelPart part : collected) {
			this.parts.add(writer.add(part));
		}
		Material.Baked particleMaterial = model.particleMaterial();
		this.sprite = writer.add(particleMaterial.sprite());
		this.forceTranslucent = particleMaterial.forceTranslucent();
	}

	@Override
	public DazyImpl export(RegistryReader reader) {
		List<DashBlockModelPart.DazyImpl> partsOut = new ArrayList<>(this.parts.size());
		for (int part : this.parts) {
			partsOut.add(reader.get(part));
		}
		return new DazyImpl(partsOut, reader.get(this.sprite), this.forceTranslucent);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBlockStateModel that = (DashBlockStateModel) o;

		if (sprite != that.sprite) return false;
		if (forceTranslucent != that.forceTranslucent) return false;
		return parts.equals(that.parts);
	}

	@Override
	public int hashCode() {
		int result = parts.hashCode();
		result = 31 * result + sprite;
		result = 31 * result + (forceTranslucent ? 1 : 0);
		return result;
	}

	public static class DazyImpl extends Dazy<BlockStateModel> {
		public final List<DashBlockModelPart.DazyImpl> parts;
		public final DashSprite.DazyImpl sprite;
		public final boolean forceTranslucent;

		public DazyImpl(List<DashBlockModelPart.DazyImpl> parts, DashSprite.DazyImpl sprite, boolean forceTranslucent) {
			this.parts = parts;
			this.sprite = sprite;
			this.forceTranslucent = forceTranslucent;
		}

		@Override
		protected BlockStateModel resolve(SpriteGetter spriteLoader) {
			List<BlockStateModelPart> partsOut = new ArrayList<>(this.parts.size());
			int materialFlags = 0;
			for (DashBlockModelPart.DazyImpl part : this.parts) {
				BlockStateModelPart resolved = part.get(spriteLoader);
				partsOut.add(resolved);
				materialFlags |= resolved.materialFlags();
			}
			return new Impl(partsOut, new Material.Baked(this.sprite.get(spriteLoader), this.forceTranslucent), materialFlags);
		}

		/** Direct {@link BlockStateModel} implementation backed by cached data. */
		public static final class Impl implements BlockStateModel {
			private final List<BlockStateModelPart> parts;
			private final Material.Baked particleMaterial;
			private final int materialFlags;

			public Impl(List<BlockStateModelPart> parts, Material.Baked particleMaterial, int materialFlags) {
				this.parts = parts;
				this.particleMaterial = particleMaterial;
				this.materialFlags = materialFlags;
			}

			@Override
			public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
				parts.addAll(this.parts);
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
				return materialFlags == impl.materialFlags && Objects.equals(parts, impl.parts) && Objects.equals(particleMaterial, impl.particleMaterial);
			}

			@Override
			public int hashCode() {
				return Objects.hash(parts, particleMaterial, materialFlags);
			}
		}
	}

	/**
	 * {@link BlockStateModel.UnbakedRoot} wrapper used to inject cached models
	 * into the vanilla bake pipeline on LOAD ({@code BlockStateModelLoader} shortcut).
	 */
	public static final class DashUnbakedGrouped implements BlockStateModel.UnbakedRoot {
		private final Dazy<? extends BlockStateModel> model;
private record SpriteGetterHolder(ModelBaker baker, SpriteGetter sprites) {
}

private static volatile SpriteGetterHolder spriteGetterHolder;


		public DashUnbakedGrouped(Dazy<? extends BlockStateModel> model) {
			this.model = model;
		}

		@Override
		public BlockStateModel bake(BlockState state, ModelBaker baker) {
			// ModelBaker no longer hands out a SpriteGetter; resolve sprites through its
			// MaterialBaker instead. All cached block sprites live in the block atlas.
			SpriteGetterHolder holder = spriteGetterHolder;
			if (holder == null || holder.baker() != baker) {
			    SpriteGetter sprites = id -> baker.materials().get(new Material(id.texture()), () -> "dashloader").sprite();
			    holder = new SpriteGetterHolder(baker, sprites);
			    spriteGetterHolder = holder;
			}
			return this.model.get(holder.sprites());
		}

		@Override
		public Object visualEqualityGroup(BlockState state) {
			return this.model;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
		}
	}

	/** Resolves {@link BakedQuad}s eagerly; used only for equality checks, never stored. */
	public static List<BakedQuad> collectQuads(BlockStateModel model) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(), parts);
		List<BakedQuad> out = new ArrayList<>();
		for (BlockStateModelPart part : parts) {
			out.addAll(part.getQuads(null));
		}
		return out;
	}
}
