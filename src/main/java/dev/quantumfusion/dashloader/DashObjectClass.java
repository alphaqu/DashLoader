package dev.quantumfusion.dashloader;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * A DashObject which is an object with adds Dash support to a target object. <br>
 * This class is very lazy as reflection is really slow
 *
 * @param <R> Raw
 * @param <D> Dashable
 */
public final class DashObjectClass<R, D extends dev.quantumfusion.dashloader.Dashable<R>> {
	private final Class<D> dashClass;

	@Nullable
	private Class<R> targetClass;
	@Nullable
	private Class<? extends dev.quantumfusion.dashloader.Dashable<?>> dashTag;
	@Nullable
	private List<Class<?>> dependencies;

	public DashObjectClass(Class<D> dashClass) {
		this.dashClass = dashClass;
	}

	public Class<D> getDashClass() {
		return this.dashClass;
	}

	// lazy
	@NotNull
	public Class<R> getTargetClass() {
		if (this.targetClass == null) {
			var dashObjectAnnotation = this.dashClass.getDeclaredAnnotation(DashObject.class);
			if (dashObjectAnnotation == null) {
				throw new RuntimeException("Registered Class " + this.dashClass.getSimpleName() + " does not have a @DashObject annotation.");
			}
			this.targetClass = (Class<R>) dashObjectAnnotation.value();
		}
		return this.targetClass;
	}

	// lazy
	@NotNull
	public Class<? extends dev.quantumfusion.dashloader.Dashable<?>> getTag() {
		if (this.dashTag == null) {
			Class<? extends dev.quantumfusion.dashloader.Dashable<?>> dashInterface = null;
			for (Class<?> anInterface : this.dashClass.getInterfaces()) {
				if (dev.quantumfusion.dashloader.Dashable.class.isAssignableFrom(anInterface)) {
					dashInterface = (Class<? extends dev.quantumfusion.dashloader.Dashable<?>>) anInterface;
					break;
				}
			}

			if (dashInterface == null) {
				throw new RuntimeException(this.dashClass.getSimpleName() + " does not have an interface that inherits Dashable");
			}


			//noinspection RedundantCast // very required
			this.dashTag = (dashInterface == ((Class<? extends dev.quantumfusion.dashloader.Dashable>) Dashable.class) ? this.dashClass : dashInterface);
		}
		return this.dashTag;
	}

	// lazy
	@NotNull
	public List<Class<?>> getDependencies() {
		if (this.dependencies == null) {
			Class<?>[] dependencies;
			var dependenciesAnnotation = this.dashClass.getDeclaredAnnotation(DashDependencies.class);
			if (dependenciesAnnotation == null) {
				dependencies = new Class[0];
			} else {
				dependencies = dependenciesAnnotation.value();
			}

			for (Class<?> dependency : dependencies) {
				if (dependency.getDeclaredAnnotation(DashObject.class) == null) {
					throw new RuntimeException(
							"Registered Class " + this.dashClass.getSimpleName() + " Dependency \"" + dependency.getSimpleName() +
									"\" does not have a @DashObject annotation and therefore is not a DashObject");
				}
			}
			this.dependencies = List.of(dependencies);
		}
		return this.dependencies;
	}
}
