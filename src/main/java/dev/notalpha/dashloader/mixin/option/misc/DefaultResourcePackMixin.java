package dev.notalpha.dashloader.mixin.option.misc;

import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.client.fs.FileSystemModule;
import dev.notalpha.dashloader.misc.FastResource;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(DefaultResourcePack.class)
public class DefaultResourcePackMixin {

	//@Inject(method = "<init>", at = @At(value = "TAIL"))
	//	private static void createCache(ResourceMetadataMap metadata, Set<String> namespaces, List<Path> rootPaths, Map<ResourceType, List<Path>> namespacePaths, CallbackInfo ci) {
	//
	//		//String key = namespace + ":" + rootPath.toString();
	//		//FileSystemModule.FILE_NODES_SAVE.visit(CacheStatus.SAVE, map -> {
	//		//	FileSystemModule.ToSave toSave = map.get(key);
	//		//	if (toSave == null) {
	//		//		map.put(key, new FileSystemModule.ToSave(namespace, rootPath));
	//		//	}
	//		//});
	//
	//	}


	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private static void collectIdentifiers(ResourcePack.ResultConsumer consumer, String namespace, Path root, List<String> prefixSegments) {
		Path path = root.resolve(namespace);
		FastResource.findResources(namespace, path, prefixSegments, consumer);
	}
}
