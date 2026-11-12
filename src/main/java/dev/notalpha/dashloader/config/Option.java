package dev.notalpha.dashloader.config;

public enum Option {
	CACHE_FONT("cache.font"),                                   // Caches fonts and their images.
	CACHE_LANGUAGE("cache.language"),                           // Caches the parsed translation files.
	CACHE_MODEL_LOADER("cache.model"),                          // Caches BakedModels which allows the game to load extremely fast
	CACHE_SPLASH_TEXT("cache.SplashTextResourceSupplierMixin"), // Caches the splash texts from the main screen
	CACHE_SPRITE_CONTENT("cache.sprite.content"),               // Caches sprite loading
	CACHE_SPRITE_STITCHING("cache.sprite.stitch"),              // Caches sprite stitching

	FAST_TRANSFORMATION_EQUALS("misc.AffineTransformationMixin"),
	UNSAFE_MIPMAP_GENERATION("misc.MipmapHelper");              // Speeds up get/set pixel operations when generating mipmaps by skipping redundant safety checks

	public final String mixinContains;

	Option(String mixinContains) {
		this.mixinContains = mixinContains;
	}
}
