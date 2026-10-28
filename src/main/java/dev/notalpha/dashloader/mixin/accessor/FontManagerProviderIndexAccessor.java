package dev.notalpha.dashloader.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.blaze3d.font.GlyphProvider;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.resources.Identifier;

@Mixin(FontManager.Preparation.class)
public interface FontManagerProviderIndexAccessor {
	@Invoker("<init>")
	static FontManager.Preparation create(Map<Identifier, List<GlyphProvider.Conditional>> providers, List<GlyphProvider> allProviders) {
		throw new AssertionError();
	}
}
