package dev.quantumfusion.dashloader.api.option;

public enum Option {
	CACHE_MODEL_LOADER(
			"cache.model",
			"Caches BakedModels and Sprite/Atlases which allows the game to load extremely fast", Impact.EXTREME),
	CACHE_FONT(
			"cache.font",
			"Caches all of the fonts and their images.", Impact.HIGH),
	CACHE_PARTICLE(
			"cache.ParticleManagerMixin",
			"Caches the Particles and its Atlas for a much better Particle loading time", Impact.MEDIUM),
	CACHE_SPLASH_TEXT(
			"cache.SplashTextResourceSupplierMixin",
			"Caches the splash texts from the main screen.", Impact.SMALL),
	CACHE_SHADER(
			"cache.shader",
			"Caches the GL Shaders.", Impact.MEDIUM),
	FAST_SOUND_FILE_SKIP(
			"misc.SoundManagerMixin",
			"Disables if file exists check on Sound Files.", Impact.HIGH),
	FAST_MODEL_IDENTIFIER_EQUALS(
			"misc.ModelIdentifierMixin",
			"Use a much faster .equals() on the ModelIdentifiers", Impact.MEDIUM),
	FAST_STATE_INIT(
			"state",
			"Highly unsafe but makes blocks init way faster.", Impact.MEDIUM),
	FAST_WALL_BLOCK(
			"WallBlockMixin",
			"Caches the 2 most common blockstates for wall blocks.", Impact.MEDIUM),
	FAST_UNICODE_FONT_GET_ID(
			"misc.UnicodeTextureFontMixin",
			"Use a much faster .getId() on Unicode fonts", Impact.HIGH),
	FAST_RESOURCE_PATH(
			"misc.DirectoryResourcePack",
			"Use a way faster of getting the path.", Impact.HIGH),
	WATERMARK(
			"misc.TitleScreenMixin",
			"DashLoader watermark. If you want to remove this please mind supporting me at https://ko-fi.com/notequalalpha instead and help this project grow.", Impact.NONE);

	public final String mixinContains;
	public final String description;
	public final Impact impact;

	Option(String mixinContains, String description, Impact impact) {
		this.mixinContains = mixinContains;
		this.description = description;
		this.impact = impact;
	}
}
