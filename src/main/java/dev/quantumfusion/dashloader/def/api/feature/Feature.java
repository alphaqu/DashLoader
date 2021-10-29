package dev.quantumfusion.dashloader.def.api.feature;


@SuppressWarnings("SpellCheckingInspection")
public enum Feature {
	/**
	 * Cache the modelLoader which includes Atlases and BlockStates.
	 */
	MODEL_LOADER("cache.BakedModelManagerOverride", Impact.EXTREME, 3),
	/**
	 * Cache particles including its atlas.
	 */
	PARTICLES("cache.ParticleManagerMixin", Impact.MEDIUM, 1),
	/**
	 * Cache fonts.
	 */
	FONTS("cache.FontManagerOverride", Impact.HIGH, 1),
	/**
	 * Cache the splash text in the main menu.
	 */
	SPLASH_TEXT("cache.SplashTextResourceSupplierMixin", Impact.SMALL, 1),
	/**
	 * Cache shaders.
	 */
	SHADERS("shader", Impact.MEDIUM, 1),
	/**
	 * If DashLoader should disable the file.exist check on the sound engine.
	 */
	SOUND_DISABLE_EXIST_CHECK("misc.SoundManagerMixin", Impact.HIGH, 0),
	/**
	 * if DashLoader should use a faster .equals() method for ModelIdentifiers
	 */
	MODELIDENTIFIER_FASTEQUALS("misc.ModelIdentifierMixin", Impact.MEDIUM, 0),
	/**
	 * if DashLoader should use a much faster getId() method for UnicodeTextureFonts.
	 */
	UNICODETEXTUREFONT_FASTGETID("misc.UnicodeTextureFontMixin", Impact.HIGH, 0),
	/**
	 * if DashLoader should add the watermark in the main menu screen.
	 */
	WATERMARK("misc.TitleScreenMixin", Impact.NONE, 0),
	/**
	 * if DashLoader should use .getPath() instead of .getCanonicalPath() (About a 2x improvement in getResource())
	 */
	RESOURCEPACK_FASTVALIDPATH("misc.DirectoryResourcePack", Impact.HIGH, 0);


	String mixin;
	Impact impact;
	int tasks;

	Feature(String mixin, Impact impact, int tasks) {
		this.mixin = mixin;
		this.impact = impact;
		this.tasks = tasks;
	}

	public boolean active() {
		return FeatureHandler.isFeatureActive(this);
	}
}
