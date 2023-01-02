package dev.notalpha.dashloader.client.model.components;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.render.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

public class DashBakedQuadCollection implements DashObject<BakedQuadCollection> {
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
