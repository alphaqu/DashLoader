package net.oskarstrom.dashloader.def.model.components;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.model.BakedQuad;
import net.oskarstrom.dashloader.api.registry.DashRegistry;
import net.oskarstrom.dashloader.api.Dashable;
import net.oskarstrom.dashloader.api.registry.Pointer;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;


public class DashBakedQuad implements Dashable<BakedQuad> {
	@Serialize(order = 0)
	public final int[] vertexData;
	@Serialize(order = 1)
	public final int colorIndex;
	@Serialize(order = 2)
	public final DashDirectionValue face;
	@Serialize(order = 3)
	public final boolean shade;
	@Serialize(order = 4)
	public final Pointer sprite;

	public DashBakedQuad(@Deserialize("vertexData") int[] vertexData,
						 @Deserialize("colorIndex") int colorIndex,
						 @Deserialize("face") DashDirectionValue face,
						 @Deserialize("shade") boolean shade,
						 @Deserialize("sprite") Pointer sprite) {
		this.vertexData = vertexData;
		this.colorIndex = colorIndex;
		this.face = face;
		this.shade = shade;
		this.sprite = sprite;
	}

	public DashBakedQuad(BakedQuad bakedQuad, DashRegistry registry) {
		vertexData = bakedQuad.getVertexData();
		colorIndex = bakedQuad.getColorIndex();
		face = new DashDirectionValue(bakedQuad.getFace());
		shade = bakedQuad.hasShade();
		sprite = registry.add(bakedQuad.getSprite());
	}

	public BakedQuad toUndash(DashRegistry registry) {
		return new BakedQuad(vertexData, colorIndex, face.toUndash(registry), registry.get(sprite), shade);
	}
}
