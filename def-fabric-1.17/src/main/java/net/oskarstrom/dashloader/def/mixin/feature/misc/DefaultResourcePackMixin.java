package net.oskarstrom.dashloader.def.mixin.feature.misc;

import net.minecraft.resource.DefaultResourcePack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DefaultResourcePack.class)
public class DefaultResourcePackMixin {

//	private final Object2BooleanMap<Pair<ResourceType, Identifier>> containsCache = Object2BooleanMaps.synchronize(new Object2BooleanOpenHashMap<>());
//
//
//	@Inject(method = "contains", at = @At(value = "HEAD"), cancellable = true)
//	private void containsCache(ResourceType type, Identifier id, CallbackInfoReturnable<Boolean> cir) {
//		final Pair<ResourceType, Identifier> value = Pair.of(type, id);
//		if (containsCache.containsKey(value)) {
//			cir.setReturnValue(containsCache.getBoolean(value));
//		}
//	}
//
//	@Inject(method = "contains", at = @At(value = "RETURN"), cancellable = true)
//	private void addToContainsCache(ResourceType type, Identifier id, CallbackInfoReturnable<Boolean> cir) {
//		containsCache.computeBooleanIfAbsent(Pair.of(type, id), pair -> cir.getReturnValueZ());
//	}
}
