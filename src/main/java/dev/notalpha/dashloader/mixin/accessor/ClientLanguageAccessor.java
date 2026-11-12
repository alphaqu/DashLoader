package dev.notalpha.dashloader.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;

@Mixin(ClientLanguage.class)
public interface ClientLanguageAccessor {
	@Accessor
	Map<String, String> getStorage();

	@Invoker("<init>")
	static ClientLanguage create(Map<String, String> storage, boolean rightToLeft) {
		throw new AssertionError();
	}
}