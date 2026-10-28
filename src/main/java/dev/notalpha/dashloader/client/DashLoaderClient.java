package dev.notalpha.dashloader.client;

import dev.notalpha.dashloader.api.DashEntrypoint;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheFactory;
import dev.notalpha.dashloader.client.font.*;
import dev.notalpha.dashloader.client.blockstate.DashBlockState;
import dev.notalpha.dashloader.client.identifier.DashIdentifier;
import dev.notalpha.dashloader.client.identifier.DashSpriteIdentifier;
import dev.notalpha.dashloader.client.language.LanguageModule;
import dev.notalpha.dashloader.client.model.DashBlockModelPart;
import dev.notalpha.dashloader.client.model.DashBlockStateModel;
import dev.notalpha.dashloader.client.model.DashWeightedBlockStateModel;
import dev.notalpha.dashloader.client.model.ModelModule;
import dev.notalpha.dashloader.client.model.components.DashBakedQuad;
import dev.notalpha.dashloader.client.model.components.DashBakedQuadCollection;
import dev.notalpha.dashloader.client.splash.SplashModule;
import dev.notalpha.dashloader.client.sprite.content.DashImage;
import dev.notalpha.dashloader.client.sprite.content.DashSprite;
import dev.notalpha.dashloader.client.sprite.content.DashSpriteContents;
import dev.notalpha.dashloader.client.sprite.content.SpriteContentModule;
import dev.notalpha.dashloader.client.sprite.stitch.SpriteStitcherModule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import java.nio.file.Path;
import java.util.List;

public class DashLoaderClient implements DashEntrypoint {
	public static final Cache CACHE;

	static {
		CacheFactory cacheManagerFactory = CacheFactory.create();
		List<DashEntrypoint> entryPoints = FabricLoader.getInstance().getEntrypoints("dashloader", DashEntrypoint.class);
		for (DashEntrypoint entryPoint : entryPoints) {
			entryPoint.onDashLoaderInit(cacheManagerFactory);
		}

		CACHE = cacheManagerFactory.build(Path.of("./dashloader-cache/client/"));
	}

	@Override
	public void onDashLoaderInit(CacheFactory factory) {
		factory.addModule(new FontModule());
		factory.addModule(new LanguageModule());
		factory.addModule(new ModelModule());
		factory.addModule(new SplashModule());
		factory.addModule(new SpriteStitcherModule());
		factory.addModule(new SpriteContentModule());

		factory.addMissingHandler(Identifier.class, (identifier, registryWriter) -> new DashIdentifier(identifier));

		factory.addMissingHandler(
				TextureAtlasSprite.class,
				DashSprite::new
		);

		factory.addMissingHandler(
				BlockState.class,
				(state, registryWriter) -> new DashBlockState(state, registryWriter)
		);

		//noinspection unchecked
		for (Class<? extends DashObject<?, ?>> aClass : new Class[]{
				DashIdentifier.class,
				DashBlockModelPart.class,
				DashBlockStateModel.class,
				DashWeightedBlockStateModel.class,
				DashBakedQuad.class,
				DashBakedQuadCollection.class,
				DashImage.class,
				DashSprite.class,
				DashSpriteIdentifier.class,
				DashSpriteContents.class,
				DashBitmapFont.class,
				DashBlankFont.class,
				DashSpaceFont.class,
				DashTrueTypeFont.class,
				DashUnihexFont.class,
				DashFontFilterPair.class,
				DashBlockState.class,
		}) {
			factory.addDashObject(aClass);
		}
	}
}

