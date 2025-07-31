package cn.ksmcbrigade.dashloader;

import dev.notalpha.dashloader.client.DashLoaderClient;

public final class Dashloader {
    public static final String MOD_ID = "dashloader";

    public static void init() {
        System.out.println("DashLoaderForge loading...");
        System.out.println("DashLoader need reload: "+DashLoaderClient.NEEDS_RELOAD);
    }
}
