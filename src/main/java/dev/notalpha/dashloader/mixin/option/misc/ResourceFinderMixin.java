package dev.notalpha.dashloader.mixin.option.misc;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(ResourceFinder.class)
public class ResourceFinderMixin {

	@Shadow @Final private String directoryName;

	@Shadow @Final private String fileExtension;

	///**
	// * @author
	// * @reason
	// */
	//@Overwrite
	//public Map<Identifier, Resource> findResources(ResourceManager resourceManager) {
	//	Map<Identifier, Resource> resources = resourceManager.findResources(this.directoryName, path -> path.getPath().endsWith(this.fileExtension));
//
	//	//Set<Identifier> identifiers = resources.keySet();
////
	//	//Map<Identifier, Resource> myLove = new HashMap<>();
	//	//for (Identifier identifier : identifiers) {
	//	//	Optional<Resource> resource = resourceManager.getResource(identifier);
	//	//	myLove.put(identifier, resource.get());
	//	//}
	//	return myLove;
	//}
}
