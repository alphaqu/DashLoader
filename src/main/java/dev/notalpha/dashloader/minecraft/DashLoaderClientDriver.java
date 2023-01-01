package dev.notalpha.dashloader.minecraft;

import dev.notalpha.dashloader.api.DashEntrypoint;
import dev.notalpha.dashloader.cache.CacheManager;
import dev.notalpha.dashloader.cache.CacheManagerFactory;
import dev.notalpha.dashloader.cache.registry.factory.MissingHandler;
import dev.notalpha.dashloader.minecraft.blockstate.DashBlockState;
import dev.notalpha.dashloader.minecraft.font.*;
import dev.notalpha.dashloader.minecraft.identifier.DashIdentifier;
import dev.notalpha.dashloader.minecraft.identifier.DashModelIdentifier;
import dev.notalpha.dashloader.minecraft.model.*;
import dev.notalpha.dashloader.minecraft.model.components.DashBakedQuad;
import dev.notalpha.dashloader.minecraft.model.components.DashBakedQuadCollection;
import dev.notalpha.dashloader.minecraft.model.fallback.DashMissingDashModel;
import dev.notalpha.dashloader.minecraft.model.predicates.*;
import dev.notalpha.dashloader.minecraft.shader.DashShader;
import dev.notalpha.dashloader.minecraft.shader.ShaderCacheHandler;
import dev.notalpha.dashloader.minecraft.splash.SplashTextCacheHandler;
import dev.notalpha.dashloader.minecraft.sprite.DashImage;
import dev.notalpha.dashloader.minecraft.sprite.DashSpriteImpl;
import dev.notalpha.dashloader.minecraft.sprite.SpriteCacheHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.List;

public class DashLoaderClientDriver implements DashEntrypoint {
	public static final CacheManager MANAGER;
	public static boolean NEEDS_RELOAD = false;

	static {
		CacheManagerFactory cacheManagerFactory = new CacheManagerFactory();
		List<DashEntrypoint> entryPoints = FabricLoader.getInstance().getEntrypoints("dashloader", DashEntrypoint.class);
		for (DashEntrypoint entryPoint : entryPoints) {
			entryPoint.onDashLoaderInit(cacheManagerFactory);
		}

		MANAGER = cacheManagerFactory.build(Path.of("./dashloader-cache/client/"));
	}

	@Override
	public void onDashLoaderInit(CacheManagerFactory factory) {
		factory.addCacheHandler(new FontCacheHandler());
		factory.addCacheHandler(new ModelCacheHandler());
		factory.addCacheHandler(new ShaderCacheHandler());
		factory.addCacheHandler(new SplashTextCacheHandler());
		factory.addCacheHandler(new SpriteCacheHandler());

		for (Class<?> aClass : new Class[]{
				DashIdentifier.class,
				DashModelIdentifier.class,
				DashBasicBakedModel.class,
				DashBuiltinBakedModel.class,
				DashMultipartBakedModel.class,
				DashWeightedBakedModel.class,
				DashMissingDashModel.class,
				DashBakedQuad.class,
				DashBakedQuadCollection.class,
				DashAndPredicate.class,
				DashOrPredicate.class,
				DashSimplePredicate.class,
				DashStaticPredicate.class,
				DashImage.class,
				DashSpriteImpl.class,
				DashBitmapFont.class,
				DashBlankFont.class,
				DashSpaceFont.class,
				DashTrueTypeFont.class,
				DashUnicodeFont.class,
				DashBlockState.class,
				DashShader.class
		}) {
			factory.addDashObject(aClass);
		}
	}

	@Override
	public void onDashLoaderSave(List<MissingHandler<?>> handlers) {
		handlers.add(new MissingHandler<>(
				BakedModel.class,
				(bakedModel, registryWriter) -> {
					var map = ModelCacheHandler.MISSING_WRITE.get(CacheManager.Status.SAVE);
					if (map == null) {
						return null;
					}

					if (map.containsKey(bakedModel)) {
						return map.get(bakedModel);
					}

					final DashMissingDashModel value = new DashMissingDashModel();
					map.put(bakedModel, value);
					return value;
				}
		));
		handlers.add(new MissingHandler<>(
				Identifier.class,
				(identifier, registryWriter) -> {
					if (identifier instanceof ModelIdentifier m) {
						return new DashModelIdentifier(m);
					} else {
						return new DashIdentifier(identifier);
					}
				}
		));
		handlers.add(new MissingHandler<>(
				MultipartModelSelector.class,
				(selector, writer) -> {
					if (selector == MultipartModelSelector.TRUE) {
						return new DashStaticPredicate(true);
					} else if (selector == MultipartModelSelector.FALSE) {
						return new DashStaticPredicate(false);
					} else if (selector instanceof AndMultipartModelSelector s) {
						return new DashAndPredicate(s, writer);
					} else if (selector instanceof OrMultipartModelSelector s) {
						return new DashOrPredicate(s, writer);
					} else if (selector instanceof SimpleMultipartModelSelector s) {
						return new DashSimplePredicate(s, writer);
					} else if (selector instanceof BooleanSelector s) {
						return new DashStaticPredicate(s.selector);
					} else {
						throw new RuntimeException("someone is having fun with lambda selectors again");
					}
				}
		));
	}
}
