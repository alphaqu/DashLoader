package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.Dazy;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

import java.util.Arrays;

public final class DashBakedQuad implements DashObject<BakedQuad, DashBakedQuad.DazyImpl> {
	public final float[] positions;
	public final long[] uvs;
	public final int colorIndex;
	public final Direction face;
	public final boolean shade;
	public final int sprite;
	public final int lightEmission;

	public DashBakedQuad(float[] positions, long[] uvs, int colorIndex, Direction face, boolean shade,
	                     int sprite, int lightEmission) {
		this.positions = positions;
		this.uvs = uvs;
		this.colorIndex = colorIndex;
		this.face = face;
		this.shade = shade;
		this.sprite = sprite;
		this.lightEmission = lightEmission;
	}

	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this.positions = new float[]{
				bakedQuad.position0().x(), bakedQuad.position0().y(), bakedQuad.position0().z(),
				bakedQuad.position1().x(), bakedQuad.position1().y(), bakedQuad.position1().z(),
				bakedQuad.position2().x(), bakedQuad.position2().y(), bakedQuad.position2().z(),
				bakedQuad.position3().x(), bakedQuad.position3().y(), bakedQuad.position3().z()
		};
		this.uvs = new long[]{bakedQuad.packedUV0(), bakedQuad.packedUV1(), bakedQuad.packedUV2(), bakedQuad.packedUV3()};
		this.colorIndex = bakedQuad.tintIndex();
		this.face = bakedQuad.face();
		this.shade = bakedQuad.shade();
		this.sprite = writer.add(bakedQuad.sprite());
		this.lightEmission = bakedQuad.lightEmission();
	}

	public DazyImpl export(RegistryReader handler) {
		return new DazyImpl(this.positions, this.uvs, this.colorIndex, this.face, this.shade, handler.get(this.sprite), this.lightEmission);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBakedQuad that = (DashBakedQuad) o;

		if (colorIndex != that.colorIndex) return false;
		if (shade != that.shade) return false;
		if (sprite != that.sprite) return false;
		if (lightEmission != that.lightEmission) return false;
		if (!Arrays.equals(positions, that.positions)) return false;
		if (!Arrays.equals(uvs, that.uvs)) return false;
		return face == that.face;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(positions);
		result = 31 * result + Arrays.hashCode(uvs);
		result = 31 * result + colorIndex;
		result = 31 * result + face.hashCode();
		result = 31 * result + (shade ? 1 : 0);
		result = 31 * result + sprite;
		result = 31 * result + lightEmission;
		return result;
	}

	public static class DazyImpl extends Dazy<BakedQuad> {
		public final float[] positions;
		public final long[] uvs;
		public final int colorIndex;
		public final Direction face;
		public final boolean shade;
		public final DashSprite.DazyImpl sprite;
		public final int lightEmission;

		public DazyImpl(float[] positions, long[] uvs, int colorIndex, Direction face, boolean shade, DashSprite.DazyImpl sprite, int lightEmission) {
			this.positions = positions;
			this.uvs = uvs;
			this.colorIndex = colorIndex;
			this.face = face;
			this.shade = shade;
			this.sprite = sprite;
			this.lightEmission = lightEmission;
		}

		@Override
		protected BakedQuad resolve(ErrorCollectingSpriteGetter spriteLoader) {
			Sprite sprite = this.sprite.get(spriteLoader);
			return new BakedQuad(
					new Vector3f(positions[0], positions[1], positions[2]),
					new Vector3f(positions[3], positions[4], positions[5]),
					new Vector3f(positions[6], positions[7], positions[8]),
					new Vector3f(positions[9], positions[10], positions[11]),
					uvs[0], uvs[1], uvs[2], uvs[3],
					colorIndex, face, sprite, shade, lightEmission);
		}
	}
}
