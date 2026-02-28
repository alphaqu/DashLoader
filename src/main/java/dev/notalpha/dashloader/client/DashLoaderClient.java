package dev.notalpha.dashloader.client;

import dev.notalpha.dashloader.api.DashEntrypoint;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.cache.Cache;
import dev.notalpha.dashloader.api.cache.CacheFactory;
import dev.notalpha.dashloader.client.atlas.AtlasModule;
import dev.notalpha.dashloader.client.blockstate.DashBlockState;
import dev.notalpha.dashloader.client.font.*;
import dev.notalpha.dashloader.client.identifier.DashIdentifier;
import dev.notalpha.dashloader.client.identifier.DashSpriteIdentifier;
import dev.notalpha.dashloader.client.model.ModelModule;
import dev.notalpha.dashloader.client.model.predicates.*;
import dev.notalpha.dashloader.client.shader.ShaderModule;
import dev.notalpha.dashloader.client.splash.SplashModule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.multipart.CombinedCondition;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;

public class DashLoaderClient implements DashEntrypoint {
	public static final Cache CACHE;
	public static boolean NEEDS_RELOAD = false;

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
		factory.addModule(new AtlasModule());
		factory.addModule(new FontModule());
		factory.addModule(new ModelModule());
		factory.addModule(new ShaderModule());
		factory.addModule(new SplashModule());

		factory.addMissingHandler(ResourceLocation.class, (identifier, registryWriter) -> new DashIdentifier(identifier));
		factory.addMissingHandler(Material.class, DashSpriteIdentifier::new);
		factory.addMissingHandler(
				Condition.class,
				(selector, writer) -> {
					if (selector instanceof CombinedCondition s && s.operation() == CombinedCondition.Operation.AND) {
						return new DashAndPredicate(s, writer);
					} else if (selector instanceof CombinedCondition s && s.operation() == CombinedCondition.Operation.OR) {
						return new DashOrPredicate(s, writer);
					} else if (selector instanceof KeyValueCondition s) {
						return new DashSimplePredicate(s);
					} else if (selector instanceof BooleanSelector s) {
						return new DashStaticPredicate(s.selector);
					} else {
						throw new RuntimeException("someone is having fun with lambda selectors again");
					}
				}
		);

		//noinspection unchecked
		for (Class<? extends DashObject<?, ?>> aClass : new Class[]{
				DashIdentifier.class,
				DashSpriteIdentifier.class,
				DashAndPredicate.class,
				DashOrPredicate.class,
				DashSimplePredicate.class,
				DashStaticPredicate.class,
				DashBitmapFont.class,
				DashBlankFont.class,
				DashSpaceFont.class,
				DashTrueTypeFont.class,
				DashUnihexFont.class,
				DashFontFilterPair.class,
				DashBlockState.class
		}) {
			factory.addDashObject(aClass);
		}
	}
}
