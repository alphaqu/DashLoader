package dev.notalpha.dashloader.client.fs;

import com.google.common.base.Joiner;
import dev.notalpha.dashloader.misc.FastResource;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FsNode {
	private static final Joiner SEPARATOR_JOINER = Joiner.on("/");
	@Nullable
	public final List<FsNode> folderChildren;
	@Nullable
	public final List<FsFileNode> fileChildren;
	public final String name;

	public FsNode(@Nullable List<FsNode> folderChildren, @Nullable List<FsFileNode> fileChildren, String name) {
		this.folderChildren = folderChildren;
		this.fileChildren = fileChildren;
		this.name = name;
	}

	public static FsNode scan(Path root, Path path, String namespace, String name) {
		List<FsNode> folderChildren = new ArrayList<>();
		List<FsFileNode> fileChildren = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			stream.forEach(foundPath -> {
				if (path == foundPath) {
					return;
				}
				String string = foundPath.getFileName().toString();
				if (Files.isDirectory(foundPath)) {
					folderChildren.add(FsNode.scan(root, foundPath, namespace, string));
				} else if (Files.isRegularFile(foundPath)) {
					String identPath = SEPARATOR_JOINER.join(root.relativize(foundPath));
					Identifier identifier = Identifier.of(namespace, identPath);
					if (identifier == null) {
						Util.error(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", namespace, identPath));
					} else {
						fileChildren.add(new FsFileNode(string, identifier));
					}
				}
			});
		} catch (NotDirectoryException | NoSuchFileException ignored) {
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new FsNode(folderChildren.isEmpty() ? null : folderChildren, fileChildren.isEmpty() ? null : fileChildren, name);
	}

	public void findResources(List<String> prefixSegments, ResourcePack.ResultConsumer consumer) {
		// Find node through the prefix segments
		FsNode node = this;
		hello:
		for (String prefixSegment : prefixSegments) {
			if (node.folderChildren != null) {
				for (FsNode child : node.folderChildren) {
					if (Objects.equals(child.name, prefixSegment)) {
						node = child;
						continue hello;
					}
				}
			}


			//		System.out.println("Could not find path to " + prefixSegment);
			return;
		}

		// Walk the node tree
		node.walkFiles(consumer);
	}

	public void computePaths(Path prev, boolean root) {
		Path path;
		if (root) {
			path = prev;
		} else {
			path = prev.resolve(this.name);
		}

		if (this.folderChildren != null) {
			for (FsNode folderChild : this.folderChildren) {
				folderChild.computePaths(path, false);
			}
		}

		if (this.fileChildren != null) {
			for (FsFileNode fileChild : this.fileChildren) {
				fileChild.computePath(path);
			}
		}
	}

	public void walkFiles(ResourcePack.ResultConsumer consumer) {
		if (this.fileChildren != null) {
			for (FsFileNode fileChild : this.fileChildren) {
				consumer.accept(fileChild.identifier, InputSupplier.create(fileChild.path));
			}
		}

		if (this.folderChildren != null) {
			for (FsNode folderChild : this.folderChildren) {
				folderChild.walkFiles(consumer);
			}
		}
	}
}
