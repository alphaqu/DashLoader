package dev.notalpha.dashloader.client.fs;

import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.hyphen.scan.annotations.DataNullable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DashNode {
	@Nullable
	@DataNullable
	public final List<DashNode> folderChildren;
	@Nullable
	@DataNullable
	public final List<DashFileNode> fileChildren;
	public final String name;

	public DashNode(@Nullable List<DashNode> folderChildren, @Nullable List<DashFileNode> fileChildren, String name) {
		this.folderChildren = folderChildren;
		this.fileChildren = fileChildren;
		this.name = name;
	}

	public DashNode(RegistryWriter writer, FsNode node) {
		List<DashNode> folderChildren = null;
		if (node.folderChildren != null) {
			folderChildren = new ArrayList<>();
			for (FsNode folderChild : node.folderChildren) {
				folderChildren.add(new DashNode(writer, folderChild));
			}
		}
		this.folderChildren = folderChildren;

		List<DashFileNode> fileChildren = null;
		if (node.fileChildren != null) {
			fileChildren = new ArrayList<>();
			for (FsFileNode fileChild : node.fileChildren) {
				fileChildren.add(new DashFileNode(writer, fileChild));
			}
		}
		this.fileChildren = fileChildren;

		this.name = node.name;
	}

	public FsNode export(RegistryReader reader)  {
		List<FsNode> folderChildren = null;
		if (this.folderChildren != null) {
			folderChildren = new ArrayList<>();
			for (DashNode folderChild : this.folderChildren) {
				folderChildren.add(folderChild.export(reader));
			}
		}

		List<FsFileNode> fileChildren = null;
		if (this.fileChildren != null) {
			fileChildren = new ArrayList<>();
			for (DashFileNode fileChild : this.fileChildren) {
				fileChildren.add(fileChild.export(reader));
			}
		}

		return new FsNode(folderChildren, fileChildren, this.name);
	}
}
