package cn.ksmcbrigade.dashloader.forge;

import cn.ksmcbrigade.dashloader.Dashloader;
import net.minecraftforge.fml.common.Mod;

@Mod(Dashloader.MOD_ID)
public final class DashloaderForge {
    public DashloaderForge() {
        // Submit our event bus to let Architectury API register our content on the right time.

        // Run our common setup.
        Dashloader.init();
    }
}
