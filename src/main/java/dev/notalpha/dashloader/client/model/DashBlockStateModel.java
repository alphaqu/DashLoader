package dev.notalpha.dashloader.client.model;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cached form of a {@link BlockStateModel} (1.21.5+ model system).
 * Every model (simple, weighted-resolved, multipart-resolved) is stored as its
 * resolved {@link BlockModelPart}s plus particle sprite. Weighted models keep
 * their variants separately in {@link DashWeightedBlockStateModel} so vanilla
 * random picking is preserved.
 */
public final class DashBlockStateModel implements DashObject<BlockStateModel, DashBlockStateModel.DazyImpl> {
	public final List<Integer> parts;
	public final int sprite;

	public DashBlockStateModel(List<Integer> parts, int sprite) {
		this.parts = parts;
		this.sprite = sprite;
	}

	public DashBlockStateModel(BlockStateModel model, RegistryWriter writer) {
		List<BlockModelPart> collected = new ArrayList<>();
		model.collectParts(RandomSource.create(), collected);
		this.parts = new ArrayList<>(collected.size());
		for (BlockModelPart part : collected) {
			this.parts.add(writer.add(part));
		}
		this.sprite = writer.add(model.particleIcon());
	}

	@Override
	public DazyImpl export(RegistryReader reader) {
		List<DashBlockModelPart.DazyImpl> partsOut = new ArrayList<>(this.parts.size());
		for (int part : this.parts) {
			partsOut.add(reader.get(part));
		}
		return new DazyImpl(partsOut, reader.get(this.sprite));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBlockStateModel that = (DashBlockStateModel) o;

		if (sprite != that.sprite) return false;
		return parts.equals(that.parts);
	}

	@Override
	public int hashCode() {
		int result = parts.hashCode();
		result = 31 * result + sprite;
		return result;
	}

	public static class DazyImpl extends Dazy<BlockStateModel> {
		public final List<DashBlockModelPart.DazyImpl> parts;
		public final DashSprite.DazyImpl sprite;

		public DazyImpl(List<DashBlockModelPart.DazyImpl> parts, DashSprite.DazyImpl sprite) {
			this.parts = parts;
			this.sprite = sprite;
		}

		@Override
		protected BlockStateModel resolve(SpriteGetter spriteLoader) {
			List<BlockModelPart> partsOut = new ArrayList<>(this.parts.size());
			for (DashBlockModelPart.DazyImpl part : this.parts) {
				partsOut.add(part.get(spriteLoader));
			}
			return new Impl(partsOut, this.sprite.get(spriteLoader));
		}

		/** Direct {@link BlockStateModel} implementation backed by cached data. */
		public static final class Impl implements BlockStateModel {
			private final List<BlockModelPart> parts;
			private final TextureAtlasSprite sprite;

			public Impl(List<BlockModelPart> parts, TextureAtlasSprite sprite) {
				this.parts = parts;
				this.sprite = sprite;
			}

			@Override
			public void collectParts(RandomSource random, List<BlockModelPart> parts) {
				parts.addAll(this.parts);
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
				return Objects.equals(parts, impl.parts) && Objects.equals(sprite, impl.sprite);
			}

			@Override
			public int hashCode() {
				return Objects.hash(parts, sprite);
			}
		}
	}

	/**
	 * {@link BlockStateModel.UnbakedRoot} wrapper used to inject cached models
	 * into the vanilla bake pipeline on LOAD ({@link BlockStatesLoader} shortcut).
	 */
	public static final class DashUnbakedGrouped implements BlockStateModel.UnbakedRoot {
		private final Dazy<? extends BlockStateModel> model;

		public DashUnbakedGrouped(Dazy<? extends BlockStateModel> model) {
			this.model = model;
		}

		@Override
		public BlockStateModel bake(BlockState state, ModelBaker baker) {
			return this.model.get(baker.sprites());
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
		List<BlockModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(), parts);
		List<BakedQuad> out = new ArrayList<>();
		for (BlockModelPart part : parts) {
			out.addAll(part.getQuads(null));
		}
		return out;
	}
}
