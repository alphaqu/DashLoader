package dev.quantumfusion.dashloader.minecraft.model.components;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

@DashObject(BakedQuadCollection.class)
public class DashBakedQuadCollection implements Dashable<BakedQuadCollection> {
	public final List<Integer> quads;

	public DashBakedQuadCollection(List<Integer> quads) {
		this.quads = quads;
	}

	public DashBakedQuadCollection(BakedQuadCollection quads, RegistryWriter writer) {
		this.quads = new ArrayList<>();
		for (BakedQuad quad : quads.quads) {
			this.quads.add(writer.add(quad));
		}
	}

	@Override
	public BakedQuadCollection export(RegistryReader reader) {
		var out = new ArrayList<BakedQuad>(this.quads.size());
		for (Integer quad : this.quads) {
			out.add(reader.get(quad));
		}
		return new BakedQuadCollection(out);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashBakedQuadCollection that = (DashBakedQuadCollection) o;

		return quads.equals(that.quads);
	}

	@Override
	public int hashCode() {
		return quads.hashCode();
	}
}
