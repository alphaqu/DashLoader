package dev.notalpha.dashloader.mixin.option.misc;

import dev.notalpha.dashloader.client.fs.input.PathInputSupplier;
import dev.notalpha.dashloader.client.fs.input.ZipInputSupplier;
import net.minecraft.resource.InputSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(InputSupplier.class)
public interface InputSupplierMixin {

	@Overwrite
	static InputSupplier<InputStream> create(Path path) {
		return new PathInputSupplier(path);
	}

	@Overwrite
	static InputSupplier<InputStream> create(ZipFile zipFile, ZipEntry zipEntry) {
		return new ZipInputSupplier(zipFile, zipEntry);
	}
}
