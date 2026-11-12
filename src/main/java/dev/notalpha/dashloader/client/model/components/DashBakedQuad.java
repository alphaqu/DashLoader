package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import org.joml.Vector3f;

import java.util.Arrays;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.Direction;

public final class DashBakedQuad implements DashObject<BakedQuad, DashBakedQuad.DazyImpl> {
	public final float[] positions;
	public final long[] uvs;
	public final int tintIndex;
	public final Direction face;
	public final int shadeDirectionOverride;
	public final int sprite;
	public final int lightEmission;
	public final ChunkSectionLayer layer;

	public DashBakedQuad(float[] positions, long[] uvs, int tintIndex, Direction face, int shadeDirectionOverride,
	                     int sprite, int lightEmission, ChunkSectionLayer layer) {
		this.positions = positions;
		this.uvs = uvs;
		this.tintIndex = tintIndex;
		this.face = face;
		this.shadeDirectionOverride = shadeDirectionOverride;
		this.sprite = sprite;
		this.lightEmission = lightEmission;
		this.layer = layer;
	}

	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this.positions = new float[]{
				bakedQuad.position0().x(), bakedQuad.position0().y(), bakedQuad.position0().z(),
				bakedQuad.position1().x(), bakedQuad.position1().y(), bakedQuad.position1().z(),
				bakedQuad.position2().x(), bakedQuad.position2().y(), bakedQuad.position2().z(),
				bakedQuad.position3().x(), bakedQuad.position3().y(), bakedQuad.position3().z()
		};
		this.uvs = new long[]{bakedQuad.packedUV0(), bakedQuad.packedUV1(), bakedQuad.packedUV2(), bakedQuad.packedUV3()};
		BakedQuad.MaterialInfo materialInfo = bakedQuad.materialInfo();
		this.tintIndex = materialInfo.tintIndex();
		this.face = bakedQuad.direction();
		Direction shadeOverride = materialInfo.shadeDirectionOverride();
		this.shadeDirectionOverride = shadeOverride == null ? -1 : shadeOverride.ordinal();
		this.sprite = writer.add(materialInfo.sprite());
		this.lightEmission = materialInfo.lightEmission();
		this.layer = materialInfo.layer();
	}

	public DazyImpl export(RegistryReader handler) {
		return new DazyImpl(this.positions, this.uvs, this.tintIndex, this.face, this.shadeDirectionOverride, handler.get(this.sprite), this.lightEmission, this.layer);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBakedQuad that = (DashBakedQuad) o;

		if (tintIndex != that.tintIndex) return false;
		if (shadeDirectionOverride != that.shadeDirectionOverride) return false;
		if (sprite != that.sprite) return false;
		if (lightEmission != that.lightEmission) return false;
		if (!Arrays.equals(positions, that.positions)) return false;
		if (!Arrays.equals(uvs, that.uvs)) return false;
		if (face != that.face) return false;
		return layer == that.layer;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(positions);
		result = 31 * result + Arrays.hashCode(uvs);
		result = 31 * result + tintIndex;
		result = 31 * result + face.hashCode();
		result = 31 * result + shadeDirectionOverride;
		result = 31 * result + sprite;
		result = 31 * result + lightEmission;
		result = 31 * result + layer.hashCode();
		return result;
	}

	public static class DazyImpl extends Dazy<BakedQuad> {
		public final float[] positions;
		public final long[] uvs;
		public final int tintIndex;
		public final Direction face;
		public final int shadeDirectionOverride;
		public final DashSprite.DazyImpl sprite;
		public final int lightEmission;
		public final ChunkSectionLayer layer;

		public DazyImpl(float[] positions, long[] uvs, int tintIndex, Direction face, int shadeDirectionOverride, DashSprite.DazyImpl sprite, int lightEmission, ChunkSectionLayer layer) {
			this.positions = positions;
			this.uvs = uvs;
			this.tintIndex = tintIndex;
			this.face = face;
			this.shadeDirectionOverride = shadeDirectionOverride;
			this.sprite = sprite;
			this.lightEmission = lightEmission;
			this.layer = layer;
		}

		@Override
		protected BakedQuad resolve(SpriteGetter spriteLoader) {
			TextureAtlasSprite sprite = this.sprite.get(spriteLoader);
			boolean translucent = this.layer == ChunkSectionLayer.TRANSLUCENT;
			BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
					sprite, this.layer,
					itemRenderType(sprite, translucent, false),
					itemRenderType(sprite, translucent, true),
					itemGlintSpecialRenderType(sprite, translucent),
					this.tintIndex,
					this.shadeDirectionOverride < 0 ? null : Direction.values()[this.shadeDirectionOverride],
					this.lightEmission);
			return new BakedQuad(
					new Vector3f(positions[0], positions[1], positions[2]),
					new Vector3f(positions[3], positions[4], positions[5]),
					new Vector3f(positions[6], positions[7], positions[8]),
					new Vector3f(positions[9], positions[10], positions[11]),
					uvs[0], uvs[1], uvs[2], uvs[3],
					face, materialInfo);
		}

		/**
		 * Rebuilds the item render types exactly as {@link BakedQuad.MaterialInfo#of} derives them:
		 * they only depend on {@code transparency.hasTranslucent()}, which {@link ChunkSectionLayer}
		 * encodes ({@code TRANSLUCENT} iff translucent), plus whether the sprite lives in the block atlas.
		 */
		private static RenderType itemRenderType(TextureAtlasSprite sprite, boolean translucent, boolean glint) {
			if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
				if (glint) {
					return translucent ? Sheets.translucentBlockItemGlintSheet() : Sheets.cutoutBlockItemGlintSheet();
				}
				return translucent ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
			}
			if (glint) {
				return translucent ? Sheets.translucentItemGlintSheet() : Sheets.cutoutItemGlintSheet();
			}
			return translucent ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
		}

		private static RenderType itemGlintSpecialRenderType(TextureAtlasSprite sprite, boolean translucent) {
			if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
				return translucent ? Sheets.translucentBlockItemGlintSpecialSheet() : Sheets.cutoutBlockItemGlintSpecialSheet();
			}
			return translucent ? Sheets.translucentItemGlintSpecialSheet() : Sheets.cutoutItemGlintSpecialSheet();
		}
	}
}
