package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashDirectionValue;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;

@Data
@DashObject(BakedQuad.class)
public record DashBakedQuad(int[] vertexData, int colorIndex, DashDirectionValue face, boolean shade, int sprite) implements Dashable<BakedQuad> {
	public DashBakedQuad(BakedQuad bakedQuad, DashRegistryWriter writer) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), new DashDirectionValue(bakedQuad.getFace()), bakedQuad.hasShade(), writer.add(bakedQuad.getSprite()));
	}

	public BakedQuad export(DashRegistryReader handler) {
		return new BakedQuad(vertexData, colorIndex, face.export(handler), handler.get(sprite), shade);
	}
}
