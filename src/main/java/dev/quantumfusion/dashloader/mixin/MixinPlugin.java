package dev.quantumfusion.dashloader.mixin;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.option.ConfigHandler;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
		DashLoader.bootstrap();
		ConfigHandler.update();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return ConfigHandler.shouldApplyMixin(mixinClassName);
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
		//if (targetClassName.equals("net.minecraft.client.render.model.ModelLoader")) {
		//	for (MethodNode method : targetClass.methods) {
		//		if (method.name.endsWith("onFinishAddingModels")) {
		//			for (int i = 0; i < 20; i++) {
		//				DashLoader.LOGGER.info("YOUR MOM");
		//			}
		//			method.instructions.clear();
		//			method.instructions.add(new InsnNode(Opcodes.RETURN));
		//		}
		//	}
		//}
	}
}
