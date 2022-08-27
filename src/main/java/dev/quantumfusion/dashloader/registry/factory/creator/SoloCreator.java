package dev.quantumfusion.dashloader.registry.factory.creator;

import dev.quantumfusion.dashloader.DashObjectClass;
import dev.quantumfusion.dashloader.Dashable;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class SoloCreator<R, D extends Dashable<R>> implements Creator<R, D> {
	private final MethodHandle method;
	private final FactoryFunction creator;

	private SoloCreator(MethodHandle method, FactoryFunction creator) {
		this.method = method;
		this.creator = creator;
	}

	public static <R, D extends Dashable<R>> SoloCreator<R, D> create(DashObjectClass<R, D> dashObject) {
		final Class<?> dashClass = dashObject.getDashClass();

		var creator = scanCreators((look, type) -> look.findConstructor(dashClass, type.changeReturnType(void.class)), dashObject);
		if (creator == null) {
			creator = scanCreators((look, type) -> look.findStatic(dashClass, "factory", type), dashObject);
		}
		if (creator == null) {
			creator = scanCreators((look, type) -> look.findStatic(dashClass, "factory", type), dashObject);
		}

		if (creator == null) {
			throw new RuntimeException("Could not find a way to create " + dashClass.getSimpleName() + ". Create the method and/or check if it's accessible.");
		}

		return creator;
	}

	@Nullable
	private static <R, D extends Dashable<R>> SoloCreator<R, D> scanCreators(MethodTester tester, DashObjectClass<R, D> dashObject) {
		for (InvokeType value : InvokeType.values()) {
			final Class<?>[] apply = value.parameters.apply(dashObject);

			try {
				var method = tester.getMethod(
						MethodHandles.publicLookup(),
						MethodType.methodType(dashObject.getTargetClass(), apply));

				if (method != null) {
					return new SoloCreator<>(method, value.creator);
				}
			} catch (Throwable ignored) {
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public D create(R raw, RegistryWriter writer) throws Throwable {
		return (D) this.creator.create(this.method, raw, writer);
	}


	// FULL object, writer
	// WRITER writer
	// RAW object
	// EMPTY
	private enum InvokeType {
		FULL((methodHandle, args, args2) -> methodHandle.invoke(args, args2), doc -> new Class[]{doc.getTargetClass(), RegistryWriter.class}),
		WRITER((mh, raw, writer) -> mh.invoke(writer), doc -> new Class[]{RegistryWriter.class}),
		RAW((mh, raw, writer) -> mh.invoke(raw), doc -> new Class[]{doc.getTargetClass()}),
		EMPTY((mh, raw, writer) -> mh.invoke(), doc -> new Class[0]);
		private final FactoryFunction creator;
		private final Function<DashObjectClass<?, ?>, Class<?>[]> parameters;

		InvokeType(FactoryFunction creator, Function<DashObjectClass<?, ?>, Class<?>[]> parameters) {
			this.creator = creator;
			this.parameters = parameters;
		}
	}

	@FunctionalInterface
	private interface FactoryFunction {
		Object create(MethodHandle method, Object raw, RegistryWriter writer) throws Throwable;
	}

	@FunctionalInterface
	private interface MethodTester {
		MethodHandle getMethod(MethodHandles.Lookup lookup, MethodType parameters) throws Throwable;
	}

}
