package dev.notalpha.dashloader.client.fs;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.DashModule;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.taski.builtin.StepTask;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileSystemModule implements DashModule<FileSystemModule.Data> {
	public static final CachingData<Map<String, ToSave>> FILE_NODES_SAVE = new CachingData<>(CacheStatus.SAVE);
	public static final CachingData<Map<String, ToLoad>> FILE_NODES = new CachingData<>(CacheStatus.LOAD);
	@Override
	public void reset(Cache cache) {
		FILE_NODES_SAVE.reset(cache, new HashMap<>());
		FILE_NODES.reset(cache, new HashMap<>());
	}

	@Override
	public Data save(RegistryWriter writer, StepTask task) {
		var nodes = FILE_NODES_SAVE.get(CacheStatus.SAVE);
		assert nodes != null;
		Map<String, DashNode> cache = new HashMap<>();
		task.doForEach(nodes, (s, node) -> {
			cache.put(s, new DashNode(writer, FsNode.scan(node.rootPath, node.rootPath, node.namespace, node.rootPath.toString())));
		});
		return new Data(cache);
	}

	@Override
	public void load(Data data, RegistryReader reader, StepTask task) {
		var nodes = FILE_NODES.get(CacheStatus.LOAD);
		assert nodes != null;
		nodes.clear();

		task.doForEach(data.cache, (s, dashFileNode) -> {
			nodes.put(s, new ToLoad(dashFileNode.export(reader), false));
		});
	}

	@Override
	public float taskWeight() {
		return 10;
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	public static class Data {
		public final Map<String, DashNode> cache;

		public Data(Map<String, DashNode> cache) {
			this.cache = cache;
		}
	}

	public record ToSave(String namespace, Path rootPath) {}

	public static final class ToLoad {
		public final FsNode node;
		public volatile boolean computedPaths;

		public ToLoad(FsNode node, boolean computedPaths) {
			this.node = node;
			this.computedPaths = computedPaths;
		}
	}
}
