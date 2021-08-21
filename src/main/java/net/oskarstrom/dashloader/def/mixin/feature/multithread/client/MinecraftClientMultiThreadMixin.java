package net.oskarstrom.dashloader.def.mixin.feature.multithread.client;//package net.quantumfusion.dashloader.mixin.feature.multithread.client;
//
//import com.mojang.authlib.minecraft.MinecraftSessionService;
//import com.mojang.datafixers.DataFixer;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.RunArgs;
//import net.minecraft.client.font.FontManager;
//import net.minecraft.client.option.GameOptions;
//import net.minecraft.client.resource.FoliageColormapResourceSupplier;
//import net.minecraft.client.resource.GrassColormapResourceSupplier;
//import net.minecraft.client.resource.SplashTextResourceSupplier;
//import net.minecraft.client.resource.language.LanguageManager;
//import net.minecraft.client.sound.MusicTracker;
//import net.minecraft.client.sound.SoundManager;
//import net.minecraft.client.texture.PlayerSkinProvider;
//import net.minecraft.client.texture.TextureManager;
//import net.minecraft.client.util.Session;
//import net.minecraft.resource.ReloadableResourceManager;
//import net.minecraft.resource.ResourceManager;
//import net.minecraft.world.level.storage.LevelStorage;
//import net.quantumfusion.dashloader.util.ThreadHelper;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Mutable;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.Redirect;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.io.File;
//import java.nio.file.Path;
//
//@Mixin(MinecraftClient.class)
//public class MinecraftClientMultiThreadMixin {
//
//    @Shadow
//    @Final
//    public File runDirectory;
//    @Shadow
//    @Final
//    public GameOptions options;
//    @Shadow
//    @Final
//    @Mutable
//    private TextureManager textureManager;
//    @Shadow
//    @Final
//    @Mutable
//    private PlayerSkinProvider skinProvider;
//    @Shadow
//    @Final
//    @Mutable
//    private LevelStorage levelStorage;
//    @Shadow
//    @Final
//    @Mutable
//    private SoundManager soundManager;
//    @Shadow
//    @Final
//    @Mutable
//    private SplashTextResourceSupplier splashTextLoader;
//    @Shadow
//    @Final
//    @Mutable
//    private MusicTracker musicTracker;
//    @Shadow
//    @Final
//    private MinecraftSessionService sessionService;
//    @Shadow
//    @Final
//    private DataFixer dataFixer;
//    @Shadow
//    @Final
//    private ReloadableResourceManager resourceManager;
//    @Shadow
//    @Final
//    private Session session;
//
//
//    @Mutable
//    @Shadow
//    @Final
//    private LanguageManager languageManager;
//    private GrassColormapResourceSupplier grassColormapResourceSupplier;
//    private FoliageColormapResourceSupplier foliageColormapResourceSupplier;
//
//    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;addResourcePackProfilesToManager(Lnet/minecraft/resource/ResourcePackManager;)V", shift = At.Shift.AFTER))
//    private void firstParallelInject(RunArgs args, CallbackInfo ci) {
//        System.out.println("First run");
//        FontManager.MISSING_STORAGE_ID.toString();
//        ThreadHelper.exec(
//                () -> languageManager = new LanguageManager(options.language),
//                () -> textureManager = new TextureManager(this.resourceManager)
//        );
//        ThreadHelper.exec(
//                () -> skinProvider = new PlayerSkinProvider(this.textureManager, new File(args.directories.assetDir, "skins"), this.sessionService),
//                () -> levelStorage = new LevelStorage(this.runDirectory.toPath().resolve("saves"), this.runDirectory.toPath().resolve("backups"), this.dataFixer),
//                () -> soundManager = new SoundManager(resourceManager, options),
//                () -> splashTextLoader = new SplashTextResourceSupplier(session),
//                () -> musicTracker = new MusicTracker((MinecraftClient) (Object) this),
//                () -> grassColormapResourceSupplier = new GrassColormapResourceSupplier(),
//                () -> foliageColormapResourceSupplier = new FoliageColormapResourceSupplier()
//        );
//    }
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/resource/language/LanguageManager;"
//            )
//    )
//    public LanguageManager languageManagerThread(String languageCode) {
//        return languageManager;
//    }
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/texture/TextureManager;"
//            )
//    )
//    public TextureManager textureManagerThread(ResourceManager resourceManager) {
//        return textureManager;
//    }
//
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/texture/PlayerSkinProvider;"
//            )
//    )
//    public PlayerSkinProvider playerSkinProviderThread(TextureManager textureManager, File skinCacheDir, final MinecraftSessionService sessionService) {
//        return skinProvider;
//    }
//
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/world/level/storage/LevelStorage;"
//            )
//    )
//    public LevelStorage levelStorageThread(Path savesDirectory, Path backupsDirectory, DataFixer dataFixer) {
//        return levelStorage;
//    }
//
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/sound/SoundManager;"
//            )
//    )
//    public SoundManager soundManagerThread(ResourceManager resourceManager, GameOptions gameOptions) {
//        return soundManager;
//    }
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/resource/SplashTextResourceSupplier;"
//            )
//    )
//    public SplashTextResourceSupplier splashTextResourceSupplierThread(Session session) {
//        return splashTextLoader;
//    }
//
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/sound/MusicTracker;"
//            )
//    )
//    public MusicTracker musicTrackerThread(MinecraftClient client) {
//        return musicTracker;
//    }
//
////    @SuppressWarnings("UnresolvedMixinReference")
////    @Redirect(
////            method = "<init>",
////            at = @At(
////                    value = "NEW",
////                    target = "(Lnet/minecraft/client/texture/TextureManager;)Lnet/minecraft/client/font/FontManager;"
////            )
////    )
////    public FontManager fontManagerThread(TextureManager manager) {
////        return fontManager;
////    }
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/resource/GrassColormapResourceSupplier;"
//            )
//    )
//    public GrassColormapResourceSupplier grassColormapResourceSupplierThread() {
//        return grassColormapResourceSupplier;
//    }
//
//    @SuppressWarnings("UnresolvedMixinReference")
//    @Redirect(
//            method = "<init>",
//            at = @At(
//                    value = "NEW",
//                    target = "Lnet/minecraft/client/resource/FoliageColormapResourceSupplier;"
//            )
//    )
//    public FoliageColormapResourceSupplier foliageColormapResourceSupplierThread() {
//        return foliageColormapResourceSupplier;
//    }
//
//
//}
