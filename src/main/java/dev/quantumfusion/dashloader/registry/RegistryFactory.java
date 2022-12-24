package dev.quantumfusion.dashloader.registry;

import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.api.DashObjectClass;
import dev.quantumfusion.dashloader.registry.chunk.WriteChunk;
import dev.quantumfusion.dashloader.registry.factory.DashFactory;
import dev.quantumfusion.dashloader.registry.factory.MissingHandler;

import java.util.*;
import java.util.function.Function;

public final class RegistryFactory {
	private static <R, D extends Dashable<R>> List<DashObjectClass<R, D>> calculateBuildOrder(List<Holder> elements) {
		final int elementsSize = elements.size();
		final var mapping = new HashMap<Class<?>, Holder>();

		for (var element : elements) {
			mapping.put(element.self, element);
		}

		for (var element : elements) {
			for (var dependency : element.dependencies) {
				Holder oHolder = mapping.get(dependency);
				if (oHolder == null) {
					throw new RuntimeException(element.self + " has a dependency  " + dependency + " which does not exist.");
				}
				oHolder.references++;
			}
		}

		var queue = new ArrayDeque<Holder>(elementsSize);
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
		List<DashObjectClass<R, D>> out = new ArrayList<>(outArray.length);
		for (Holder holder : outArray) {
			out.add(0, (DashObjectClass<R, D>) holder.object);
		}

		return out;
	}

	public <R, D extends Dashable<R>> RegistryWriter createWriter(List<MissingHandler<?>> missingHandlers, Collection<DashObjectClass<?, ?>> dashObjects) {
		HashMap<Class<?>, List<Class<?>>> dashTagLookup = new HashMap<>();
		for (DashObjectClass<?, ?> dashObject : dashObjects) {
			dashTagLookup.computeIfAbsent(dashObject.getTag(), aClass -> new ArrayList<>()).add(dashObject.getDashClass());
		}


		List<DashObjectClass<R, D>> groups = calculateBuildOrder(Holder.map(dashObjects, raw -> {
			DashObjectClass<R, D> dashObject = (DashObjectClass<R, D>) raw;
			// Process dependencies and make categories point to actual objects.

			Set<Class<?>> dependenciesOut = new HashSet<>();
			for (Class<?> dependency : dashObject.getDependencies()) {
				if (dependency.isInterface()) {
					List<Class<?>> members = dashTagLookup.get(dependency);
					if (members == null) {
						throw new RuntimeException("DashObject " + dashObject.getDashClass() + " has a category dependency " + dependency + " which does not exist");
					}
					dependenciesOut.addAll(members);
				} else {
					dependenciesOut.add(dependency);
				}
			}

			// Remove self reference if pointing to the same category.
			dependenciesOut.remove(dashObject.getDashClass());


			return new Holder(dashObject.getDashClass(), dependenciesOut, dashObject);
		}));


		//noinspection unchecked
		WriteChunk<R, D>[] chunks = new WriteChunk[groups.size()];
		RegistryWriter writer = new RegistryWriter(chunks, missingHandlers);

		if (groups.size() > 63) {
			throw new RuntimeException("Hit group limit of 63. Please contact QuantumFusion if you hit this limit!");
		}

		for (int i = 0; i < groups.size(); i++) {
			final DashObjectClass<R, D> dashObject = groups.get(i);
			DashFactory<R, D> factory = DashFactory.create(dashObject);
			var name = dashObject.getDashClass().getSimpleName();

			System.out.println(i + " / " + name);
			var writeChunk = new WriteChunk<>((byte) i, name, writer, factory, dashObject);
			chunks[i] = writeChunk;
			writer.addChunkMapping(dashObject.getDashClass(), (byte) i);
		}

		return writer;
	}

	private static class Holder {
		public final DashObjectClass<?, ?> object;
		private final Class<?> self;
		private final Collection<Class<?>> dependencies;
		private int references = 0;

		public Holder(Class<?> self, Collection<Class<?>> dependencies, DashObjectClass<?, ?> object) {
			this.self = self;
			this.dependencies = dependencies;
			this.object = object;
		}

		public static <I> List<Holder> map(Collection<I> objects, Function<I, Holder> mapper) {
			List<Holder> holders = new ArrayList<>();
			objects.forEach(object -> holders.add(mapper.apply(object)));
			return holders;
		}
	}
}
