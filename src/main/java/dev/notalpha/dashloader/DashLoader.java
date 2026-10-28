package dev.notalpha.dashloader;

import dev.notalpha.dashloader.io.Serializer;
import dev.notalpha.dashloader.io.data.CacheInfo;
import dev.notalpha.dashloader.platform.LoaderAdapter;
import dev.notalpha.dashloader.platform.LoaderAdapter.ModInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DashLoader {
	public static final Logger LOG = LogManager.getLogger("DashLoader");
	public static final Serializer<CacheInfo> METADATA_SERIALIZER = new Serializer<>(CacheInfo.class);
	public static final String MOD_HASH;
	private static final String VERSION = LoaderAdapter.getModVersion("dashloader");

	static {
		List<ModInfo> versions = new ArrayList<>(LoaderAdapter.getAllMods());

		versions.sort(Comparator.comparing(ModInfo::id));

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < versions.size(); i++) {
			ModInfo metadata = versions.get(i);
			stringBuilder.append(i).append("$").append(metadata.id()).append('&').append(metadata.version());
		}

		MOD_HASH = DigestUtils.md5Hex(stringBuilder.toString()).toUpperCase();
	}

	private DashLoader() {
		LOG.info("Initializing DashLoader {}.", VERSION);
		if (LoaderAdapter.isDevelopmentEnvironment()) {
			LOG.warn("DashLoader launched in dev.");
		}
	}

	@SuppressWarnings("EmptyMethod")
	public static void bootstrap() {
	}
}
