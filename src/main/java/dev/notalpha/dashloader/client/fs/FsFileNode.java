package dev.notalpha.dashloader.client.fs;

import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class FsFileNode {
	public transient Path path;
	public final Identifier identifier;
	public final String name;

	public FsFileNode(String name, Identifier identifier) {
		this.name = name;
		this.identifier = identifier;
	}

	public void computePath(Path prev) {
		this.path = prev.resolve(name);
	}
}
