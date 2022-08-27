package dev.quantumfusion.dashloader.mixin.option.misc;

import com.google.common.base.CharMatcher;
import java.io.File;
import net.minecraft.resource.DirectoryResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
	 * @reason _
	 */
	@Overwrite
	public static boolean isValidPath(File file, String filename) {
		String string = file.getPath();
		if (IS_WINDOWS) {
			string = BACKSLASH_MATCHER.replaceFrom(string, '/');
		}
		return string.endsWith(filename);
	}
}
