package dev.notalpha.dashloader.mixin.accessor;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(targets = "net.minecraft.client.gui.font.FontManager$Preparation")
public interface FontManagerPreparationAccessor {
	@Invoker("<init>")
	static Object create(Map<ResourceLocation, List<GlyphProvider.Conditional>> fontSets, List<GlyphProvider> allProviders) {
		throw new AssertionError();
	}

	@Accessor("fontSets")
	Map<ResourceLocation, List<GlyphProvider.Conditional>> getFontSets();

	@Accessor("allProviders")
	List<GlyphProvider> getAllProviders();
}
