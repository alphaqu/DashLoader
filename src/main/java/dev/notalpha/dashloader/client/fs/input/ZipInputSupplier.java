package dev.notalpha.dashloader.client.fs.input;

import net.minecraft.resource.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipInputSupplier implements InputSupplier<InputStream> {
	private final ZipFile zipFile;
	private final ZipEntry zipEntry;

	public ZipInputSupplier(ZipFile zipFile, ZipEntry zipEntry) {
		this.zipFile = zipFile;
		this.zipEntry = zipEntry;
	}

	@Override
	public InputStream get() throws IOException {
		return zipFile.getInputStream(zipEntry);
	}
}
