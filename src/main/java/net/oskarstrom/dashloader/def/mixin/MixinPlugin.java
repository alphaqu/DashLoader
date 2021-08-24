package net.oskarstrom.dashloader.def.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
		//TODO config
/*		FeatureHandler.init();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			final ModMetadata metadata = mod.getMetadata();
			if (metadata.containsCustomValue("dashloader:disablefeature")) {
				final CustomValue customValue = metadata.getCustomValue("dashloader:disablefeature");
				customValue.getAsArray().forEach(value -> {
					final String feature = value.getAsString();
					FeatureHandler.disableFeature(feature);
					DashLoader.LOGGER.warn("Disabled " + feature + " feature from mod: " + metadata.getName());
				});
			}
		}
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		final Path resolve = DashLoader.getConfig().resolve("dashloader.yaml");
		if (Files.exists(resolve)) {
			try {
				resolve.toFile().setReadable(true);
				final DashConfig dashConfig = yaml.loadAs(FileUtils.openInputStream(resolve.toFile()), DashConfig.class);
				for (Feature feature : dashConfig.getDisabledFeatures()) {
					FeatureHandler.disableFeature(feature);
					DashLoader.LOGGER.error("Disabled " + feature + " feature from config");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				resolve.toFile().setWritable(true);
				Files.createFile(resolve);
				final DashConfig data = new DashConfig(new Feature[]{});
				Files.writeString(resolve, yaml.dumpAs(data, Tag.MAP, null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		//TODO config / features
		return true /*FeatureHandler.active(mixinClassName)*/;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
