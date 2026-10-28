package dev.notalpha.dashloader.client;

import dev.notalpha.dashloader.client.ui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge replacement for the Fabric-only {@code ModMenuCompat} ModMenu entrypoint.
 *
 * <p>The existing {@link ConfigScreen} is plain vanilla (no ModMenu or Cloth Config
 * dependencies), so it is reused as-is — this factory only adapts it to NeoForge's
 * {@link IConfigScreenFactory} extension point.
 *
 * <p>Register from the NeoForge client mod constructor (see the mod entrypoint workstream):
 * <pre>{@code
 * container.registerExtensionPoint(IConfigScreenFactory.class, new DashLoaderConfigScreenFactory());
 * }</pre>
 */
public class DashLoaderConfigScreenFactory implements IConfigScreenFactory {
	@Override
	public Screen createScreen(ModContainer container, Screen modListScreen) {
		return new ConfigScreen(modListScreen);
	}
}
