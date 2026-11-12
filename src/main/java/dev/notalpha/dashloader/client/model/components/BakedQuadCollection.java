package dev.notalpha.dashloader.client.model.components;

import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class BakedQuadCollection {
	public final List<BakedQuad> quads;

	public BakedQuadCollection(List<BakedQuad> quads) {
		this.quads = quads;
	}
}
