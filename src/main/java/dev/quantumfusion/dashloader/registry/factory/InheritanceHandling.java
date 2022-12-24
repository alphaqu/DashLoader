package dev.quantumfusion.dashloader.registry.factory;

public enum InheritanceHandling {
	/**
	 * Super handling is not allowed.
	 */
	FORBIDDEN,
	/**
	 * Super handling is allowed if the super class does not add any fields.
	 */
	ALLOWED_NO_FIELDS,
	/**
	 * Super handling is allowed even if the super class adds fields.
	 */
	ALLOWED_WITH_FIELDS
}
