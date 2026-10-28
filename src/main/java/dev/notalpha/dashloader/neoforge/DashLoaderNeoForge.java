package dev.notalpha.dashloader.neoforge;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.client.DashLoaderClient;
import dev.notalpha.dashloader.client.DashLoaderConfigScreenFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge mod entrypoint (client-only).
 */
@Mod(value = "dashloader", dist = Dist.CLIENT)
public class DashLoaderNeoForge {
	public DashLoaderNeoForge(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, new DashLoaderConfigScreenFactory());
		DashLoaderClient.init();
		DashLoader.LOG.info("DashLoader NeoForge init ({})", DashLoaderClient.CACHE);
	}
}
