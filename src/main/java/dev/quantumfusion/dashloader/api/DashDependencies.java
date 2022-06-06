package dev.quantumfusion.dashloader.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation declares dependencies that you register through a {@link dev.quantumfusion.dashloader.registry.RegistryWriter}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DashDependencies {
	/**
	 * All the DashObjects you depend on.
	 */
	Class<?>[] value();
}
