package dev.notalpha.dashloader.client.font;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.config.ConfigHandler;
import dev.notalpha.dashloader.config.Option;
import dev.notalpha.taski.builtin.StepTask;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.util.freetype.FT_Face;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontModule implements DashModule<FontModule.Data> {
public static final CachingData<ProviderIndex> DATA = new CachingData<>();
public static final CachingData<Map<FT_Face, DashTrueTypeFont.FontPrams>> FONT_TO_DATA = new CachingData<>();

@Override
public void reset(Cache cache) {
DATA.reset(cache, new ProviderIndex(new HashMap<>(), new ArrayList<>()));
FONT_TO_DATA.reset(cache, new HashMap<>());
}

@Override
public Data save(RegistryWriter factory, StepTask task) {
ProviderIndex providerIndex = DATA.get(CacheStatus.SAVE);
assert providerIndex != null;

int taskSize = 0;
for (List<GlyphProvider.Conditional> value : providerIndex.providers.values()) {
taskSize += value.size();
}
taskSize += providerIndex.allProviders.size();
task.reset(taskSize);

var providers = new IntObjectList<List<Integer>>();
providerIndex.providers.forEach((identifier, fontFilterPairs) -> {
var values = new ArrayList<Integer>();
for (GlyphProvider.Conditional fontFilterPair : fontFilterPairs) {
values.add(factory.add(fontFilterPair));
task.next();
}
providers.put(factory.add(identifier), values);
});

var allProviders = new ArrayList<Integer>();
for (GlyphProvider allProvider : providerIndex.allProviders) {
allProviders.add(factory.add(allProvider));
task.next();
}

return new Data(new DashProviderIndex(providers, allProviders));
}

@Override
public void load(Data data, RegistryReader reader, StepTask task) {
ProviderIndex index = new ProviderIndex(new HashMap<>(), new ArrayList<>());
data.fontMap.providers.forEach((key, value) -> {
var fonts = new ArrayList<GlyphProvider.Conditional>();
for (Integer i : value) {
fonts.add(reader.get(i));
}
index.providers.put(reader.get(key), fonts);
});

data.fontMap.allProviders.forEach((value) -> index.allProviders.add(reader.get(value)));
DATA.set(CacheStatus.LOAD, index);
}

@Override
public Class<Data> getDataClass() {
return Data.class;
}

@Override
public boolean isActive() {
return ConfigHandler.optionActive(Option.CACHE_FONT);
}

public static final class Data {
public final DashProviderIndex fontMap;

public Data(DashProviderIndex fontMap) {
this.fontMap = fontMap;
}
}

public static final class DashProviderIndex {
public final IntObjectList<List<Integer>> providers;
public final List<Integer> allProviders;

public DashProviderIndex(IntObjectList<List<Integer>> providers, List<Integer> allProviders) {
this.providers = providers;
this.allProviders = allProviders;
}
}

public static final class ProviderIndex {
public final Map<ResourceLocation, List<GlyphProvider.Conditional>> providers;
public final List<GlyphProvider> allProviders;

public ProviderIndex(Map<ResourceLocation, List<GlyphProvider.Conditional>> providers, List<GlyphProvider> allProviders) {
this.providers = providers;
this.allProviders = allProviders;
}
}
}
