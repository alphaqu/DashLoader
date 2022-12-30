package dev.quantumfusion.dashloader.minecraft.model.components;

import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

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
}
