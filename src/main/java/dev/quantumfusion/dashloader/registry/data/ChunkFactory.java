package dev.quantumfusion.dashloader.registry.data;

import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.api.Dashable;
import dev.quantumfusion.dashloader.minecraft.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.minecraft.model.components.DashBakedQuadCollection;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import dev.quantumfusion.dashloader.util.RegistryUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;

public class ChunkFactory<R, D extends Dashable<R>> {
	public static int C_DE = 0;
	public static int Q_DE = 0;
	public static int C_AMOUNT = 0;
	public static int Q_AMOUNT = 0;
	public final byte chunkId;
	public final String name;
	public final DashObjectClass<R, D> dashObject;
	public final List<Entry<D>> list = new ArrayList<>();
	public final Object2IntMap<D> deduplication = new Object2IntOpenHashMap<>();
	private final DashFactory<R, D> factory;

	public ChunkFactory(byte chunkId, String name, DashFactory<R, D> factory, DashObjectClass<R, D> dashObject) {
		this.chunkId = chunkId;
		this.name = name;
		this.factory = factory;
		this.dashObject = dashObject;
	}

	public D create(R raw, RegistryWriter writer) {
		return this.factory.create(raw, writer);
	}
	public int add(Entry<D> entry, RegistryFactory factory) {
		if (entry.data.getClass() == DashBakedQuadCollection.class) {
			C_AMOUNT += 1;
		}
		if (entry.data.getClass() == DashBakedQuad.class) {
			Q_AMOUNT += 1;
		}
		int existing = deduplication.getOrDefault(entry.data, -1);
		if (existing != -1) {
			if (entry.data.getClass() == DashBakedQuadCollection.class) {
				C_DE += 1;
			}

			if (entry.data.getClass() == DashBakedQuad.class) {
				Q_DE += 1;
			}
			return RegistryUtil.createId(existing, chunkId);
		}

		final int pos = this.list.size();
		this.list.add(entry);

		// Add to deduplication
		deduplication.put(entry.data, pos);

		// Increment dependencies
		for (int dependency : entry.dependencies) {
			ChunkFactory<?, ?> chunk = factory.chunks[RegistryUtil.getChunkId(dependency)];
			ChunkFactory.Entry<?> dependencyEntry = chunk.list.get(RegistryUtil.getObjectId(dependency));
			dependencyEntry.references++;
		}

		return RegistryUtil.createId(pos, chunkId);
	}

	public List<Entry<D>> getList() {
		return list;
	}

	public static final class Entry<D> {
		public final D data;
		public final int[] dependencies;
		public int references = 0;
		public int stage = -1;

		public Entry(D data, int[] dependencies) {
			this.data = data;
			this.dependencies = dependencies;
		}
	}
}
