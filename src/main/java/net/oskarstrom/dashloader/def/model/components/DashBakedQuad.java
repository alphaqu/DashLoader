package net.oskarstrom.dashloader.def.model.components;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;
import net.oskarstrom.dashloader.core.Dashable;
import net.oskarstrom.dashloader.core.registry.DashExportHandler;
import net.oskarstrom.dashloader.core.registry.DashRegistry;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;

@Data
public record DashBakedQuad(int[] vertexData, int colorIndex, DashDirectionValue face, boolean shade, int sprite) implements Dashable<BakedQuad> {
	public DashBakedQuad(BakedQuad bakedQuad, DashRegistry registry) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), new DashDirectionValue(bakedQuad.getFace()), bakedQuad.hasShade(), registry.add(bakedQuad.getSprite()));
	}

	public BakedQuad toUndash(DashExportHandler handler) {
		return new BakedQuad(vertexData, colorIndex, face.toUndash(handler), handler.get(sprite), shade);
	}
}
