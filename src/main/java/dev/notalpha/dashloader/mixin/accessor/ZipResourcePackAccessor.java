package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FilePackResources.class)
public interface ZipResourcePackAccessor {
	@Accessor
	FilePackResources.SharedZipFileAccess getZipFileAccess();
}
