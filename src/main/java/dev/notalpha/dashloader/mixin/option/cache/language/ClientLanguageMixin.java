package dev.notalpha.dashloader.mixin.option.cache.language;

import dev.notalpha.dashloader.api.CachingData;
import dev.notalpha.dashloader.api.cache.CacheStatus;
import dev.notalpha.dashloader.api.collection.ObjectObjectList;
import dev.notalpha.dashloader.client.language.LanguageModule;
import dev.notalpha.dashloader.mixin.accessor.ClientLanguageAccessor;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
	@Inject(method = "loadFrom", at = @At("HEAD"), cancellable = true)
	private static void dashloader$loadFromCache(ResourceManager resourceManager, List<String> codes,
			boolean rightToLeft, CallbackInfoReturnable<ClientLanguage> cir) {
		LanguageModule.Translations cached = LanguageModule.getLoad(codes);
		if (cached == null || cached.rightToLeft != rightToLeft) {
			return;
		}
		cir.setReturnValue(ClientLanguageAccessor.create(cached.asMap(), rightToLeft));
	}

	@Inject(method = "loadFrom", at = @At("RETURN"))
	private static void dashloader$capture(ResourceManager resourceManager, List<String> codes,
			boolean rightToLeft, CallbackInfoReturnable<ClientLanguage> cir) {
		CachingData<ObjectObjectList<String, LanguageModule.Translations>> staging =
				LanguageModule.SAVE;
		if (!staging.active(CacheStatus.SAVE)) {
			return;
		}

		ClientLanguageAccessor accessor = (ClientLanguageAccessor) cir.getReturnValue();
		Map<String, String> storage = accessor.getStorage();

		ObjectObjectList<String, String> entries = new ObjectObjectList<>();
		storage.forEach(entries::put);

		ObjectObjectList<String, LanguageModule.Translations> languages = staging.get(CacheStatus.SAVE);
		String key = LanguageModule.key(codes);
		languages.list().removeIf(entry -> entry.key().equals(key));
		languages.put(key, new LanguageModule.Translations(entries, rightToLeft));
	}
}