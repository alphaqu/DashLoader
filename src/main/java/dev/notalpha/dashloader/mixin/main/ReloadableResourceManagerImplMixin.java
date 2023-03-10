package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import net.minecraft.util.Unit;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {

	@Inject(method = "reload",
			at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void reloadDash(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> cir) {
		ProfilerUtil.RELOAD_START = System.currentTimeMillis();
		ResourcePackManager manager = MinecraftClient.getInstance().getResourcePackManager();
		List<String> values = new ArrayList<>();

		for (ResourcePackProfile profile : manager.getEnabledProfiles()) {
			if (profile != null) {
				values.add(profile.getName() + "N" + profile.getDisplayName().getString() + "D" + profile.getDescription().getString());
			}
		}

		DashLoaderClient.CACHE.setHash(DigestUtils.md5Hex(values.toString()).toUpperCase());
		DashLoaderClient.CACHE.start();
	}
}
