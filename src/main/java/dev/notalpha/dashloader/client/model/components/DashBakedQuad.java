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

import java.util.Arrays;

public final class DashBakedQuad implements DashObject<BakedQuad, DashBakedQuad.DazyImpl> {
	public final int[] vertexData;
	public final int colorIndex;
	public final Direction face;
	public final boolean shade;
	public final int sprite;
	public final int lightEmission;

	public DashBakedQuad(int[] vertexData, int colorIndex, Direction face, boolean shade,
	                     int sprite, int lightEmission) {
		this.vertexData = vertexData;
		this.colorIndex = colorIndex;
		this.face = face;
		this.shade = shade;
		this.sprite = sprite;
		this.lightEmission = lightEmission;
	}

	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this(bakedQuad.vertexData(), bakedQuad.tintIndex(), bakedQuad.face(), bakedQuad.shade(), writer.add(bakedQuad.sprite()), bakedQuad.lightEmission());
	}

	public DazyImpl export(RegistryReader handler) {
		return new DazyImpl(this.vertexData, this.colorIndex, this.face, this.shade, handler.get(this.sprite), this.lightEmission);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBakedQuad that = (DashBakedQuad) o;

		if (colorIndex != that.colorIndex) return false;
		if (shade != that.shade) return false;
		if (sprite != that.sprite) return false;
		if (!Arrays.equals(vertexData, that.vertexData)) return false;
		return face == that.face;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(vertexData);
		result = 31 * result + colorIndex;
		result = 31 * result + face.hashCode();
		result = 31 * result + (shade ? 1 : 0);
		result = 31 * result + sprite;
		return result;
	}

	public static class DazyImpl extends Dazy<BakedQuad> {
		public final int[] vertexData;
		public final int colorIndex;
		public final Direction face;
		public final boolean shade;
		public final DashSprite.DazyImpl sprite;
		public final int lightEmission;

		public DazyImpl(int[] vertexData, int colorIndex, Direction face, boolean shade, DashSprite.DazyImpl sprite, int lightEmission) {
			this.vertexData = vertexData;
			this.colorIndex = colorIndex;
			this.face = face;
			this.shade = shade;
			this.sprite = sprite;
			this.lightEmission = lightEmission;
		}

		@Override
		protected BakedQuad resolve(ErrorCollectingSpriteGetter spriteLoader) {
			Sprite sprite = this.sprite.get(spriteLoader);
			return new BakedQuad(vertexData, colorIndex, face, sprite, shade, lightEmission);
		}
	}
}
