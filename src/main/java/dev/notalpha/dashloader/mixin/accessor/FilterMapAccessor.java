package dev.notalpha.dashloader.mixin.accessor;

import net.minecraft.client.gui.font.FontOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FontOption.Filter.class)
public interface FilterMapAccessor {
@Accessor
Map<FontOption, Boolean> getValues();
}
