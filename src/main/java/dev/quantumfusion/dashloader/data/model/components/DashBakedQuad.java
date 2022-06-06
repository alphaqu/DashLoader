package dev.quantumfusion.dashloader.data.model.components;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.image.DashSprite;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

@DashObject(BakedQuad.class)
@DashDependencies(DashSprite.class)
public record DashBakedQuad(int[] vertexData, int colorIndex, Direction face, boolean shade,
							int sprite) implements Dashable<BakedQuad> {
	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.hasShade(), writer.add(bakedQuad.getSprite()));
	}

	public BakedQuad export(RegistryReader handler) {
		return new BakedQuad(this.vertexData, this.colorIndex, this.face, handler.get(this.sprite), this.shade);
	}
}
