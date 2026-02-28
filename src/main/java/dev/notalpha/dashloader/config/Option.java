package dev.notalpha.dashloader.config;

public enum Option {
	CACHE_ATLASES("cache.SpriteAtlasTextureMixin"),             // Caches stitched texture atlases, significantly reducing GPU upload time
	CACHE_FONT("cache.font"),                                   // Caches fonts and their images.
	CACHE_MODEL_LOADER("cache.model"),                          // Caches BakedModels which allows the game to load extremely fast
	CACHE_SHADER("cache.shader"),                               // Caches the GL Shaders
	CACHE_SPLASH_TEXT("cache.SplashTextResourceSupplierMixin"), // Caches the splash texts from the main screen
	CACHE_SPRITE_CONTENT("cache.sprite.content"),               // Caches sprite loading
	CACHE_SPRITE_STITCHING("cache.sprite.stitch"),              // Caches sprite stitching

	FAST_MODEL_IDENTIFIER_EQUALS("misc.ModelIdentifierMixin"),  // Use a much faster .equals() on ModelIdentifiers
	FAST_WALL_BLOCK("WallBlockMixin"),                          // Caches the two most common blockstates for wall blocks
	UNSAFE_MIPMAP_GENERATION("misc.MipmapGenerator");              // Speeds up get/set pixel operations when generating mipmaps by skipping redundant safety checks

	public final String mixinContains;

	Option(String mixinContains) {
		this.mixinContains = mixinContains;
	}
}
