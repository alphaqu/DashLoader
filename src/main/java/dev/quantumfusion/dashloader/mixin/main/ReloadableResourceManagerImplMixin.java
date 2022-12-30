package dev.quantumfusion.dashloader.mixin.main;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.ProfilerHandler;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static dev.quantumfusion.dashloader.DashLoader.INSTANCE;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {

	@Inject(method = "reload",
			at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void reloadDash(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> cir) {
		ProfilerHandler.INSTANCE.reloadStart = System.currentTimeMillis();
		INSTANCE.reload(packs.stream().map(ResourcePack::getName).collect(Collectors.toList()));
	}
}
