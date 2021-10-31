package dev.quantumfusion.dashloader.def.mixin.feature.misc;

import com.google.common.base.CharMatcher;
import net.minecraft.resource.DirectoryResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(DirectoryResourcePack.class)
public class DirectoryResourcePackMixin {


	@Shadow
	@Final
	private static boolean IS_WINDOWS;

	@Shadow
	@Final
	private static CharMatcher BACKSLASH_MATCHER;

	/**
	 * @author notequalalpha
	 */
	@Overwrite
	public static boolean isValidPath(File file, String filename) {
		String string = file.getPath();
		if (IS_WINDOWS) string = BACKSLASH_MATCHER.replaceFrom(string, '/');
		return string.endsWith(filename);
	}
}
