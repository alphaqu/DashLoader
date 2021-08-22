package net.oskarstrom.dashloader.def.util.enums;

import net.oskarstrom.dashloader.core.util.PathConstants;
import net.oskarstrom.dashloader.def.DashLoader;

import java.nio.file.Path;

public enum DashCachePaths {
	REGISTRY_CACHE("registry-data"),
	REGISTRY_MODEL_CACHE("registry-model-data"),
	REGISTRY_IMAGE_CACHE("registry-image-data"),
	MAPPINGS_CACHE("mappings-data");


	private final String path;

	DashCachePaths(String path) {
		this.path = path;
	}

	public String getFileName(boolean data) {
		return path + (data ? PathConstants.DATA_EXTENSION :  PathConstants.CACHE_EXTENSION);

	}
}
