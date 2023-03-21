package dev.notalpha.dashloader.api;

public interface RegistryReader {
	/**
	 * Gets an object back from the registry.
	 * This is used in conjunction with {@link RegistryWriter#add(Object)} to save and load objects from the registry.
	 * @param id A registry id which points to the object.
	 * @return Object which was registered by the id.
	 * @param <R> Object Type
	 */
	<R> R get(final int id);
}
