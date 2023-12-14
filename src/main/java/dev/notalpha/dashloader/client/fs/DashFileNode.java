package dev.notalpha.dashloader.client.fs;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;

public class DashFileNode {
	public final int identifier;
	public final String name;

	public DashFileNode(int identifier, String name) {
		this.identifier = identifier;
		this.name = name;
	}

	public DashFileNode(RegistryWriter writer, FsFileNode node) {
		this.identifier = writer.add(node.identifier);
		this.name = node.name;
	}

	public FsFileNode export(RegistryReader reader) {
		return new FsFileNode(name, reader.get(identifier));
	}
}
