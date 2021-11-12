package dev.quantumfusion.dashloader.def.data.model.components;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.data.image.DashSprite;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

@Data
@DashObject(BakedQuad.class)
@DashDependencies(DashSprite.class)
public record DashBakedQuad(int[] vertexData, int colorIndex, Direction face, boolean shade,
							int sprite) implements Dashable<BakedQuad> {
	public DashBakedQuad(BakedQuad bakedQuad, RegistryWriter writer) {
		this(bakedQuad.getVertexData(), bakedQuad.getColorIndex(), bakedQuad.getFace(), bakedQuad.hasShade(), writer.add(bakedQuad.getSprite()));
	}

	public BakedQuad export(RegistryReader handler) {
		return new BakedQuad(vertexData, colorIndex, face, handler.get(sprite), shade);
	}
}
