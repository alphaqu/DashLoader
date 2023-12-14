package dev.notalpha.dashloader.misc;

import com.google.common.base.Joiner;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.fs.FileSystemModule;
import dev.notalpha.dashloader.client.fs.FsNode;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePack;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastResource {
	private static final Joiner SEPARATOR_JOINER = Joiner.on("/");
	private static final Map<String, FsNode> CACHE = new HashMap<>();

	public static void findResources(String namespace, Path rootPath, List<String> prefixSegments, ResourcePack.ResultConsumer consumer) {
		String key = namespace + ":" + rootPath.toString();

		FileSystemModule.FILE_NODES_SAVE.visit(CacheStatus.SAVE, map -> {
			FileSystemModule.ToSave toSave = map.get(key);
			if (toSave == null) {
				map.put(key, new FileSystemModule.ToSave(namespace, rootPath));
			}
		});


		Map<String, FileSystemModule.ToLoad> map = FileSystemModule.FILE_NODES.get(CacheStatus.LOAD);
		if (map != null) {
			FileSystemModule.ToLoad node = map.get(key);
			if (node != null) {
				if (!node.computedPaths) {
					synchronized (node) {
						if (!node.computedPaths) {
							DashLoader.LOG.info("Computing paths for {}", key);
							node.node.computePaths(rootPath, true);
							node.computedPaths = true;
						}
					}
				}


				node.node.findResources(prefixSegments, consumer);
				return;
			}

			DashLoader.LOG.warn("Could not find cache for {}", key);
		}

		// If we dont have a cache, we find it regularly
		DirectoryResourcePack.findResources(namespace, rootPath, prefixSegments, consumer);

		//FsNode node = CACHE.get(key);
		//if (node == null) {
		//	synchronized (CACHE) {
		//		node = CACHE.get(key);
		//		if (node == null) {
		//			long start = System.currentTimeMillis();
		//			DashLoader.LOG.warn("Creating cache for {}", key);
		//			node = FsNode.scan(rootPath, rootPath, namespace, rootPath.toString());
		//			long scanTime = System.currentTimeMillis() - start;
		//			start = System.currentTimeMillis();
		//			node.computePaths(rootPath, true);
		//			long pathTime = System.currentTimeMillis() - start;
//
		//			CACHE.put(key, node);
		//			DashLoader.LOG.warn("Creating cache for {} took {}ms/{}ms", key, scanTime, pathTime);
//
		//		}
		//	}
		//}
//
		//node.findResources(prefixSegments, consumer);
	}

	//public static class FsNode {
	//		@Nullable
	//		private final List<FsNode> folderChildren;
	//		@Nullable
	//		private final List<FileNode> fileChildren;
	//		private final String name;
	//
	//		public FsNode(@Nullable List<FsNode> folderChildren, @Nullable List<FileNode> fileChildren, String name) {
	//			this.folderChildren = folderChildren;
	//			this.fileChildren = fileChildren;
	//			this.name = name;
	//		}
	//
	//		public static FsNode scan(Path root, Path path, String namespace, String name) {
	//			List<FsNode> folderChildren = new ArrayList<>();
	//			List<FileNode> fileChildren = new ArrayList<>();
	//			try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	//				stream.forEach(foundPath -> {
	//					if (path == foundPath) {
	//						return;
	//					}
	//					String string = foundPath.getFileName().toString();
	//					if (Files.isDirectory(foundPath)) {
	//						folderChildren.add(FsNode.scan(root, foundPath, namespace, string));
	//					} else if (Files.isRegularFile(foundPath)) {
	//						String identPath = SEPARATOR_JOINER.join(root.relativize(foundPath));
	//						Identifier identifier = Identifier.of(namespace, identPath);
	//						if (identifier == null) {
	//							Util.error(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", namespace, identPath));
	//						} else {
	//							fileChildren.add(new FileNode(string, identifier));
	//						}
	//					}
	//				});
	//			} catch (NotDirectoryException | NoSuchFileException ignored) {
	//			} catch (IOException e) {
	//				throw new RuntimeException(e);
	//			}
	//
	//			return new FsNode(folderChildren.isEmpty() ? null : folderChildren, fileChildren.isEmpty() ? null : fileChildren, name);
	//		}
	//
	//		public void findResources(List<String> prefixSegments, ResourcePack.ResultConsumer consumer) {
	//			// Find node through the prefix segments
	//			FsNode node = this;
	//			hello:
	//			for (String prefixSegment : prefixSegments) {
	//				if (node.folderChildren != null) {
	//					for (FsNode child : node.folderChildren) {
	//						if (Objects.equals(child.name, prefixSegment)) {
	//							node = child;
	//							continue hello;
	//						}
	//					}
	//				}
	//
	//
	//		//		System.out.println("Could not find path to " + prefixSegment);
	//				return;
	//			}
	//
	//			// Walk the node tree
	//			node.walkFiles(consumer);
	//		}
	//
	//		public void computePaths(Path prev, boolean root) {
	//			Path path;
	//			if (root) {
	//				path = prev;
	//			} else {
	//				path = prev.resolve(this.name);
	//			}
	//
	//			if (this.folderChildren != null) {
	//				for (FsNode folderChild : this.folderChildren) {
	//					folderChild.computePaths(path, false);
	//				}
	//			}
	//
	//			if (this.fileChildren != null) {
	//				for (FileNode fileChild : this.fileChildren) {
	//					fileChild.computePath(path);
	//				}
	//			}
	//		}
	//
	//		public void walkFiles(ResourcePack.ResultConsumer consumer) {
	//			if (this.fileChildren != null) {
	//				for (FileNode fileChild : this.fileChildren) {
	//					consumer.accept(fileChild.identifier, InputSupplier.create(fileChild.path));
	//				}
	//			}
	//
	//			if (this.folderChildren != null) {
	//				for (FsNode folderChild : this.folderChildren) {
	//					folderChild.walkFiles(consumer);
	//				}
	//			}
	//		}
	//	}
	//
	//	public static class FileNode {
	//		private transient Path path;
	//		private final Identifier identifier;
	//		private final String name;
	//
	//		public FileNode(String name, Identifier identifier) {
	//			this.name = name;
	//			this.identifier = identifier;
	//		}
	//
	//		public void computePath(Path prev) {
	//			this.path = prev.resolve(name);
	//		}
	//	}
}
