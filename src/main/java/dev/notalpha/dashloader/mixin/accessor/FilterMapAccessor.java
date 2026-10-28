package dev.notalpha.dashloader.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.gui.font.FontOption;

@Mixin(FontOption.Filter.class)
public interface FilterMapAccessor {
	@Accessor
	Map<FontOption, Boolean> getValues();
}
