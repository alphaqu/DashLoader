package dev.notalpha.dashloader.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import net.minecraft.server.packs.FilePackResources;

@Mixin(FilePackResources.SharedZipFileAccess.class)
public interface ZipWrapperResourcePackAccessor {
	@Accessor
	File getFile();
}
