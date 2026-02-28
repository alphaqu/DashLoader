package dev.notalpha.dashloader.client.atlas;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;

public class AtlasModule implements DashModule<AtlasModule.Data> {
	public static final CachingData<HashMap<String, ArrayList<FutureTask<NativeImage>>>> ATLASES = new CachingData<>();

	@Override
	public void reset(Cache cache) {
		ATLASES.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var cachedAtlases = ATLASES.get(CacheStatus.SAVE);
		// Not saving the atlases in the main cache, check `SpriteAtlasTextureMixin`

		if (cachedAtlases == null) {
			return null;
		}

		return new Data(cachedAtlases.keySet().toArray(new String[0]));
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask t) {
		var path = getAtlasFolder();

		HashMap<String, ArrayList<FutureTask<NativeImage>>> out = new HashMap<>();

		var maxMipLevel = Minecraft.getInstance().options.mipmapLevels().get();
		for (String atlasId : data.atlasIds) {
			var tasks = new ArrayList<FutureTask<NativeImage>>();

			for (int i = 0; i <= maxMipLevel; i++) { // don't load more atlases than needed
				Path imgPath = path.resolve(DigestUtils.md5Hex(atlasId + i).toUpperCase());
				if (!Files.exists(imgPath)) break;

				tasks.add(new FutureTask<>(() -> NativeImage.read(new FileInputStream(imgPath.toFile()))));
				Thread.startVirtualThread(tasks.getLast());
			}
			out.put(atlasId, tasks);
		}

		ATLASES.set(CacheStatus.LOAD, out);
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_ATLASES);
	}

	public static Path getAtlasFolder() {
		return DashLoaderClient.CACHE.getDir().resolve("atlases");
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	public record Data(String[] atlasIds) {
	}
}
