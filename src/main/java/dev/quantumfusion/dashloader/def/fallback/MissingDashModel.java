package dev.quantumfusion.dashloader.def.fallback;

import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.def.data.model.DashModel;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

@Data
@DashObject(NullPointerException.class)
public class MissingDashModel implements DashModel {

	@Override
	public BakedModel export(DashRegistryReader reader) {
		return new MissingDashModelWrapper();
	}

	public static class MissingDashModelWrapper implements BakedModel {
		public BakedModel actual;


		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
			return actual != null ? actual.getQuads(state, face, random) : List.of();
		}

		@Override
		public boolean useAmbientOcclusion() {
			return actual != null && actual.useAmbientOcclusion();
		}

		@Override
		public boolean hasDepth() {
			return actual != null && actual.hasDepth();
		}

		@Override
		public boolean isSideLit() {
			return actual != null && actual.isSideLit();
		}

		@Override
		public boolean isBuiltin() {
			return actual != null && actual.isBuiltin();
		}

		@Override
		public Sprite getParticleSprite() {
			return actual != null ? actual.getParticleSprite() : null;
		}

		@Override
		public ModelTransformation getTransformation() {
			return actual != null ? actual.getTransformation() : ModelTransformation.NONE;
		}

		@Override
		public ModelOverrideList getOverrides() {
			return actual != null ? actual.getOverrides() : ModelOverrideList.EMPTY;
		}
	}
}
