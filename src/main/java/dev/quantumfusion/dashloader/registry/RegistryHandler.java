package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.chunk.data.AbstractDataChunk;
import dev.quantumfusion.dashloader.registry.chunk.write.AbstractWriteChunk;
import dev.quantumfusion.dashloader.registry.chunk.write.FloatingWriteChunk;
import dev.quantumfusion.dashloader.registry.chunk.write.StagedWriteChunk;
import dev.quantumfusion.dashloader.registry.chunk.write.WriteChunk;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import java.util.*;
import java.util.function.Function;

public final class RegistryHandler {
	private static <O> List<O> calculateBuildOrder(List<Holder<O>> elements) {
		final int elementsSize = elements.size();
		final var mapping = new HashMap<Class<?>, Holder<O>>();

		for (var element : elements) {
			mapping.put(element.self, element);
		}

		for (var element : elements) {
			for (var dependency : element.dependencies) {
				mapping.get(dependency).references++;
			}
		}

		var queue = new ArrayDeque<Holder<O>>(elementsSize);
		for (var element : elements) {
			if (mapping.get(element.self).references == 0) {
				queue.offer(element);
			}
		}

		int currentPos = 0;
		var outArray = new Holder[elementsSize];
		while (!queue.isEmpty()) {
			var element = queue.poll();
			outArray[currentPos++] = element;
			for (var dependency : element.dependencies) {
				if (--mapping.get(dependency).references == 0) {
					queue.offer(mapping.get(dependency));
				}

			}
		}

		if (currentPos != elementsSize) {
			throw new IllegalArgumentException("Dependency overflow! Meaning it's https://www.youtube.com/watch?v=PGNiXGX2nLU.");
		}

		//invert and make list
		List<O> out = new ArrayList<>(outArray.length);
		//noinspection unchecked
		for (Holder<O> holder : outArray) {
			out.add(0, holder.object);
		}

		return out;
	}

	public RegistryReader createReader(ChunkHolder... holders) {
		var dataChunks = new ArrayList<AbstractDataChunk<?, ?>>();
		for (var holder : holders) {
			Collections.addAll(dataChunks, holder.getChunks());
		}
		AbstractDataChunk<?, ?>[] out = new AbstractDataChunk[dataChunks.size()];
		for (AbstractDataChunk<?, ?> dataChunk : dataChunks) {
			out[dataChunk.pos] = dataChunk;
		}
		return new RegistryReader(out);

	}

	public <R, D extends Dashable<R>> RegistryWriter createWriter(Map<Class<?>, DashFactory.FailCallback<?, ?>> callbacks, Collection<DashObjectClass<?, ?>> dashObjects) {
		Map<Class<?>, DashObjectGroup<R, D>> groups = new HashMap<>();
		for (DashObjectClass<?, ?> raw : dashObjects) {
			//noinspection unchecked
			DashObjectClass<R, D> dashObject = (DashObjectClass<R, D>) raw;
			var group = groups.computeIfAbsent(dashObject.getTag(), aClass -> new DashObjectGroup<>(dashObject.getTag()));
			group.addDashObject(dashObject);
		}


		List<DashObjectGroup<R, D>> groupOrder = calculateBuildOrder(Holder.map(groups.values(), objectGroup -> {
			objectGroup.prepareForSort(groups);
			return new Holder<>(objectGroup.dashTag, objectGroup.dependencies, objectGroup);
		}));


		//noinspection unchecked
		AbstractWriteChunk<R, D>[] chunks = new AbstractWriteChunk[groupOrder.size()];
		RegistryWriter writer = new RegistryWriter(chunks);

		if (groupOrder.size() > 63) {
			throw new RuntimeException("Hit group limit of 63. Please contact QuantumFusion if you hit this limit!");
		}

		for (int i = 0; i < groupOrder.size(); i++) {
			final DashObjectGroup<R, D> group = groupOrder.get(i);
			chunks[i] = group.createWriteChunk((byte) i, writer, callbacks);
			writer.addChunkMapping(group.dashTag, (byte) i);
		}


		writer.compileMappings();
		return writer;
	}

	private static class Holder<O> {
		public final O object;
		private final Class<?> self;
		private final Collection<Class<?>> dependencies;
		private int references = 0;

		public Holder(Class<?> self, Collection<Class<?>> dependencies, O object) {
			this.self = self;
			this.dependencies = dependencies;
			this.object = object;
		}

		public static <O> List<Holder<O>> map(Collection<O> objects, Function<O, Holder<O>> mapper) {
			List<Holder<O>> holders = new ArrayList<>();
			objects.forEach(object -> holders.add(mapper.apply(object)));
			return holders;
		}
	}

	private static final class DashObjectGroup<R, D extends Dashable<R>> {
		private final Class<? extends Dashable<?>> dashTag;
		// enforce list on stagedWriter and force collection here to prevent this used when sorted build order is required.
		private final Collection<DashObjectClass<R, D>> dashObjects;
		// dependency to source map
		private final Set<Class<?>> dependencies;


		private boolean internalReferences = false;


		private DashObjectGroup(Class<? extends Dashable<?>> dashTag, Collection<DashObjectClass<R, D>> dashObjects, Set<Class<?>> dependencies) {
			this.dashTag = dashTag;
			this.dashObjects = dashObjects;
			this.dependencies = dependencies;
		}

		public DashObjectGroup(Class<? extends Dashable<?>> dashTag) {
			this(dashTag, new ArrayList<>(), new HashSet<>());
		}

		public void addDashObject(DashObjectClass<R, D> dashObject) {
			this.dashObjects.add(dashObject);
			this.dependencies.addAll(dashObject.getDependencies());
		}

		public void prepareForSort(Map<Class<?>, DashObjectGroup<R, D>> groups) {
			// remove internal references and check if there are any
			for (DashObjectClass<R, D> group : this.dashObjects) {
				this.internalReferences |= this.dependencies.remove(group.getDashClass());
			}


			// make all dependencies targeted to a group
			for (DashObjectGroup<R, D> value : groups.values()) {
				for (DashObjectClass<R, D> dashObject : value.dashObjects) {
					final Class<D> dashClass = dashObject.getDashClass();
					if (this.dependencies.contains(dashClass)) {
						this.dependencies.remove(dashClass);
						this.dependencies.add(dashObject.getTag());
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public AbstractWriteChunk<R, D> createWriteChunk(byte pos, RegistryWriter writer, Map<Class<?>, DashFactory.FailCallback<?, ?>> callbacks) {
			var callback = ((Map<Class<?>, DashFactory.FailCallback<R, D>>) (Map<?, ?>) callbacks).getOrDefault(this.dashTag, (raw, writer1) -> {
				throw new RuntimeException("Cannot create " + raw);
			});

			DashFactory<R, D> factory = DashFactory.create(this.dashObjects, callback);

			var name = this.dashTag.getSimpleName();
			if (this.internalReferences) {
				Set<Class<?>> internalClasses = new HashSet<>();
				for (DashObjectClass<R, D> dashObject : this.dashObjects) {
					internalClasses.add(dashObject.getDashClass());
				}

				Map<DashObjectClass<R, D>, List<Class<?>>> internalDependencies = new HashMap<>();

				for (DashObjectClass<R, D> dashObject : this.dashObjects) {
					for (Class<?> dependency : dashObject.getDependencies()) {
						if (internalClasses.contains(dependency)) {
							internalDependencies.computeIfAbsent(dashObject, c -> new ArrayList<>()).add(dependency);
						}
					}
				}

				var groupsSorted = calculateBuildOrder(Holder.map(this.dashObjects, obj -> new Holder<>(obj.getDashClass(), internalDependencies.getOrDefault(obj, List.of()), obj)));
				return new StagedWriteChunk<>(pos, name, writer, groupsSorted, factory);
			} else {
				if (this.dependencies.size() == 0) {
					return new FloatingWriteChunk<>(pos, name, writer, this.dashObjects, factory);
				} else {
					return new WriteChunk<>(pos, name, writer, this.dashObjects, factory);
				}
			}
		}
	}
}
