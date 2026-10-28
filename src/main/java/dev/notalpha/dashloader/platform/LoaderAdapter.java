package dev.notalpha.dashloader.platform;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

/**
 * Loader abstraction so DashLoader compiles against NeoForge instead of Fabric Loader.
 *
 * <p>NeoForge equivalents used:
 * <ul>
 *   <li>Mod list / versions: {@link ModList}</li>
 *   <li>Config dir: {@link FMLPaths#CONFIGDIR}</li>
 *   <li>Dev environment: inverted {@link FMLEnvironment#production}</li>
 * </ul>
 */
public final class LoaderAdapter {
	private LoaderAdapter() {
	}

	/** Mod coordinates for hash computation and config handling. */
	public record ModInfo(String id, String version, String name) {
	}

	/**
	 * Returns the version string for the given mod id, or {@code "unknown"} if not present.
	 */
	public static String getModVersion(String modId) {
		return ModList.get().getModContainerById(modId)
				.map(container -> container.getModInfo().getVersion().toString())
				.orElse("unknown");
	}

	/**
	 * Returns all loaded mods as (id, version, name) tuples.
	 */
	public static List<ModInfo> getAllMods() {
		return ModList.get().getMods().stream()
				.map(info -> new ModInfo(info.getModId(), info.getVersion().toString(), info.getDisplayName()))
				.toList();
	}

	public static boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.isProduction();
	}

	/**
	 * NeoForge has no equivalent of {@code fabric.mod.json} {@code custom} values
	 * (e.g. {@code dashloader:disableoption}), so there is nothing to read here.
	 * Always returns an empty list for now; per-mod opt-outs via this mechanism
	 * are unsupported on NeoForge.
	 */
	public static List<String> getModCustomValues(String modId, String key) {
		return List.of();
	}
}
