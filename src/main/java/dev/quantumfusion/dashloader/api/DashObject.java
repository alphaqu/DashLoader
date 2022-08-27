package dev.quantumfusion.dashloader.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DashObject annotation is responsible for declaring this class as a dash mapping.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("unused")
public @interface DashObject {
	/**
	 * This is the Target Class. So if you are adding caching support to Integer, Your class would be named DashInteger and {@code @DashObject} value would be {@code Integer.class}
	 *
	 * @return Target Class
	 */
	Class<?> value();
}
