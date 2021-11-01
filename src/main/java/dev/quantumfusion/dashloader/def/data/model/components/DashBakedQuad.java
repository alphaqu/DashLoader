package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

@Data
@DashObject(BakedQuad.class)
public record DashBakedQuad(int[] vertexData, int colorIndex, Direction face, boolean shade,
							int sprite) implements Dashable<BakedQuad> {
	public DashBakedQuad(BakedQuad bakedQuad, DashRegistryWriter writer) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.hasShade(), writer.add(bakedQuad.getSprite()));
	}

	public BakedQuad export(DashRegistryReader handler) {
		return new BakedQuad(vertexData, colorIndex, face, handler.get(sprite), shade);
	}
}
