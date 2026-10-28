package dev.notalpha.dashloader.mixin.main;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.misc.ProfilerUtil;
import dev.notalpha.dashloader.mixin.accessor.ZipResourcePackAccessor;
import dev.notalpha.dashloader.mixin.accessor.ZipWrapperResourcePackAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerImplMixin {
	@Inject(method = "createReload",
			at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void reloadDash(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<PackResources> packs, CallbackInfoReturnable<ReloadInstance> cir) {
		ProfilerUtil.RELOAD_START = System.currentTimeMillis();
		PackRepository manager = Minecraft.getInstance().getResourcePackRepository();
		List<String> values = new ArrayList<>();

		// Use server resource pack display name to differentiate them across each-other
		for (PackResources pack : packs) {
			if (Objects.equals(pack.packId(), "server")) {
				if (pack instanceof FilePackResources zipResourcePack) {
					ZipResourcePackAccessor zipPack = (ZipResourcePackAccessor) zipResourcePack;
					Path path = ((ZipWrapperResourcePackAccessor) zipPack.getZipFileAccess()).getFile().toPath();
					values.add(path.toString());
				}
			}
		}

		for (Pack profile : manager.getSelectedPacks()) {
			if (profile != null) {
				// Skip server as we have a special case where we use its path instead which contains its hash
				if (!Objects.equals(profile.getId(), "server")) {
					// Hash id + title + description (ordered): pack updates usually bump
					// the version in title/description, so id-only hashing would silently
					// reuse stale sprite slots and force atlas fallback every boot.
					values.add(profile.getId() + "/" + profile.getTitle().getString() + "/" + profile.getDescription().getString() + "/");
				}
			}
		}

		String hash = DigestUtils.md5Hex(values.toString()).toUpperCase();
		DashLoader.LOG.info("Hash changed to {}", hash);
		if (DashLoaderClient.CACHE != null) {
			DashLoaderClient.CACHE.load(hash);
		}
	}
}
