package dev.notalpha.dashloader.minecraft.model.components;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.registry.RegistryWriter;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

@DashObject(BakedQuad.class)
public final class DashBakedQuad implements Dashable<BakedQuad> {
	public final int[] vertexData;
	public final int colorIndex;
	public final Direction face;
	public final boolean shade;
	public final int sprite;

	public DashBakedQuad(int[] vertexData, int colorIndex, Direction face, boolean shade,
						 int sprite) {
		this.vertexData = vertexData;
		this.colorIndex = colorIndex;
		this.face = face;
		this.shade = shade;
		this.sprite = sprite;
	}

	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.hasShade(), writer.add(bakedQuad.getSprite()));
	}

	public BakedQuad export(RegistryReader handler) {
		return new BakedQuad(this.vertexData, this.colorIndex, this.face, handler.get(this.sprite), this.shade);
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
}
