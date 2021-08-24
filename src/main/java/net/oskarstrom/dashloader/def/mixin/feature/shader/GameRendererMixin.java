package net.oskarstrom.dashloader.def.mixin.feature.shader;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.ResourceManager;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.data.VanillaData;
import net.oskarstrom.dashloader.def.image.shader.DashShader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	@Nullable
	private static Shader positionShader;
	@Shadow
	@Nullable
	private static Shader positionColorShader;
	@Shadow
	@Nullable
	private static Shader positionColorTexShader;
	@Shadow
	@Nullable
	private static Shader positionTexShader;
	@Shadow
	@Nullable
	private static Shader positionTexColorShader;
	@Shadow
	@Nullable
	private static Shader blockShader;
	@Shadow
	@Nullable
	private static Shader newEntityShader;
	@Shadow
	@Nullable
	private static Shader particleShader;
	@Shadow
	@Nullable
	private static Shader positionColorLightmapShader;
	@Shadow
	@Nullable
	private static Shader positionColorTexLightmapShader;
	@Shadow
	@Nullable
	private static Shader positionTexColorNormalShader;
	@Shadow
	@Nullable
	private static Shader positionTexLightmapColorShader;
	@Shadow
	@Nullable
	private static Shader renderTypeSolidShader;
	@Shadow
	@Nullable
	private static Shader renderTypeCutoutMippedShader;
	@Shadow
	@Nullable
	private static Shader renderTypeCutoutShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTranslucentShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTranslucentMovingBlockShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTranslucentNoCrumblingShader;
	@Shadow
	@Nullable
	private static Shader renderTypeArmorCutoutNoCullShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntitySolidShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityCutoutShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityCutoutNoNullShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityCutoutNoNullZOffsetShader;
	@Shadow
	@Nullable
	private static Shader renderTypeItemEntityTranslucentCullShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityTranslucentCullShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityTranslucentShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntitySmoothCutoutShader;
	@Shadow
	@Nullable
	private static Shader renderTypeBeaconBeamShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityDecalShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityNoOutlineShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityShadowShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityAlphaShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEyesShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEnergySwirlShader;
	@Shadow
	@Nullable
	private static Shader renderTypeLeashShader;
	@Shadow
	@Nullable
	private static Shader renderTypeWaterMaskShader;
	@Shadow
	@Nullable
	private static Shader renderTypeOutlineShader;
	@Shadow
	@Nullable
	private static Shader renderTypeArmorGlintShader;
	@Shadow
	@Nullable
	private static Shader renderTypeArmorEntityGlintShader;
	@Shadow
	@Nullable
	private static Shader renderTypeGlintTranslucentShader;
	@Shadow
	@Nullable
	private static Shader renderTypeGlintShader;
	@Shadow
	@Nullable
	private static Shader renderTypeGlintDirectShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityGlintShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEntityGlintDirectShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTextShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTextIntensityShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTextSeeThroughShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTextIntensitySeeThroughShader;
	@Shadow
	@Nullable
	private static Shader renderTypeLightningShader;
	@Shadow
	@Nullable
	private static Shader renderTypeTripwireShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEndPortalShader;
	@Shadow
	@Nullable
	private static Shader renderTypeEndGatewayShader;
	@Shadow
	@Nullable
	private static Shader renderTypeLinesShader;
	@Shadow
	@Nullable
	private static Shader renderTypeCrumblingShader;

	@Shadow
	@Final
	private Map<String, Shader> shaders;

	@Shadow
	protected abstract void clearShaders();

	@Inject(
			method = "loadShaders",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadShaders(ResourceManager manager, CallbackInfo ci) {
		if (DashLoader.getInstance().getStatus() == DashLoader.Status.LOADED) {
			dashReload();
			ci.cancel();
		}
	}

	@Inject(
			method = "loadShaders",
			at = @At(value = "TAIL"),
			cancellable = true
	)
	private void reloadEnd(ResourceManager manager, CallbackInfo ci) {
		DashLoader.getVanillaData().setShaderAssets(shaders);
	}

	private void dashReload() {
		final VanillaData vanillaData = DashLoader.getVanillaData();
		final Map<String, DashShader> shaders = DashLoader.getInstance().getMappings().shaderData.shaders;
		final int size = shaders.size();
		DashLoader.LOGGER.info("Applying {} shaders.", size);
		shaders.values().forEach(DashShader::apply);
		final Map<String, Shader> shaderData = vanillaData.getShaderData();
		DashLoader.LOGGER.info("Setting {} shaders.", size);
		blockShader = shaderData.get("block");
		newEntityShader = shaderData.get("new_entity");
		particleShader = shaderData.get("particle");
		positionShader = shaderData.get("position");
		positionColorShader = shaderData.get("position_color");
		positionColorLightmapShader = shaderData.get("position_color_lightmap");
		positionColorTexShader = shaderData.get("position_color_tex");
		positionColorTexLightmapShader = shaderData.get("position_color_tex_lightmap");
		positionTexShader = shaderData.get("position_tex");
		positionTexColorShader = shaderData.get("position_tex_color");
		positionTexColorNormalShader = shaderData.get("position_tex_color_normal");
		positionTexLightmapColorShader = shaderData.get("position_tex_lightmap_color");
		renderTypeSolidShader = shaderData.get("rendertype_solid");
		renderTypeCutoutMippedShader = shaderData.get("rendertype_cutout_mipped");
		renderTypeCutoutShader = shaderData.get("rendertype_cutout");
		renderTypeTranslucentShader = shaderData.get("rendertype_translucent");
		renderTypeTranslucentMovingBlockShader = shaderData.get("rendertype_translucent_moving_block");
		renderTypeTranslucentNoCrumblingShader = shaderData.get("rendertype_translucent_no_crumbling");
		renderTypeArmorCutoutNoCullShader = shaderData.get("rendertype_armor_cutout_no_cull");
		renderTypeEntitySolidShader = shaderData.get("rendertype_entity_solid");
		renderTypeEntityCutoutShader = shaderData.get("rendertype_entity_cutout");
		renderTypeEntityCutoutNoNullShader = shaderData.get("rendertype_entity_cutout_no_cull");
		renderTypeEntityCutoutNoNullZOffsetShader = shaderData.get("rendertype_entity_cutout_no_cull_z_offset");
		renderTypeItemEntityTranslucentCullShader = shaderData.get("rendertype_item_entity_translucent_cull");
		renderTypeEntityTranslucentCullShader = shaderData.get("rendertype_entity_translucent_cull");
		renderTypeEntityTranslucentShader = shaderData.get("rendertype_entity_translucent");
		renderTypeEntitySmoothCutoutShader = shaderData.get("rendertype_entity_smooth_cutout");
		renderTypeBeaconBeamShader = shaderData.get("rendertype_beacon_beam");
		renderTypeEntityDecalShader = shaderData.get("rendertype_entity_decal");
		renderTypeEntityNoOutlineShader = shaderData.get("rendertype_entity_no_outline");
		renderTypeEntityShadowShader = shaderData.get("rendertype_entity_shadow");
		renderTypeEntityAlphaShader = shaderData.get("rendertype_entity_alpha");
		renderTypeEyesShader = shaderData.get("rendertype_eyes");
		renderTypeEnergySwirlShader = shaderData.get("rendertype_energy_swirl");
		renderTypeLeashShader = shaderData.get("rendertype_leash");
		renderTypeWaterMaskShader = shaderData.get("rendertype_water_mask");
		renderTypeOutlineShader = shaderData.get("rendertype_outline");
		renderTypeArmorGlintShader = shaderData.get("rendertype_armor_glint");
		renderTypeArmorEntityGlintShader = shaderData.get("rendertype_armor_entity_glint");
		renderTypeGlintTranslucentShader = shaderData.get("rendertype_glint_translucent");
		renderTypeGlintShader = shaderData.get("rendertype_glint");
		renderTypeGlintDirectShader = shaderData.get("rendertype_glint_direct");
		renderTypeEntityGlintShader = shaderData.get("rendertype_entity_glint");
		renderTypeEntityGlintDirectShader = shaderData.get("rendertype_entity_glint_direct");
		renderTypeTextShader = shaderData.get("rendertype_text");
		renderTypeTextIntensityShader = shaderData.get("rendertype_text_intensity");
		renderTypeTextSeeThroughShader = shaderData.get("rendertype_text_see_through");
		renderTypeTextIntensitySeeThroughShader = shaderData.get("rendertype_text_intensity_see_through");
		renderTypeLightningShader = shaderData.get("rendertype_lightning");
		renderTypeTripwireShader = shaderData.get("rendertype_tripwire");
		renderTypeEndPortalShader = shaderData.get("rendertype_end_portal");
		renderTypeEndGatewayShader = shaderData.get("rendertype_end_gateway");
		renderTypeLinesShader = shaderData.get("rendertype_lines");
		renderTypeCrumblingShader = shaderData.get("rendertype_crumbling");
		DashLoader.LOGGER.info("Replacing {} shaders", size);
		clearShaders();
		shaderData.forEach(this.shaders::put);
		DashLoader.LOGGER.info("Shader reload complete.");

	}


}
