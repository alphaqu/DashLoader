package dev.notalpha.dashloader.client.font;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Reflection bridge for {@code BitmapProvider} internals that are private in 26.x
 * ({@code ImageDataHolder} is a private nested class, so neither direct references
 * nor Mixin accessors can name the type at compile time).
 * All lookups happen once; failures throw with a clear message instead of
 * corrupting the cache.
 */
public final class BitmapFontReflection {
	private static final Field IMAGE_DATA = field(BitmapProvider.class, "imageData");
	private static final Field HOLDER_IDENTIFIER = field(holderClass(), "identifier");
	private static final Field HOLDER_IMAGE = field(holderClass(), "image");
	private static final Constructor<?> HOLDER_CTOR = ctor(holderClass(), Identifier.class, NativeImage.class);
	private static final Constructor<?> PROVIDER_CTOR = ctor(BitmapProvider.class, holderClass(), CodepointMap.class);
	private static final Constructor<BitmapProvider.Glyph> GLYPH_CTOR = glyphCtor();

	private BitmapFontReflection() {
	}

	private static Class<?> holderClass() {
		try {
			Class<?> clazz = Class.forName("net.minecraft.client.gui.font.providers.BitmapProvider$ImageDataHolder");
			clazz.getDeclaredConstructor(Identifier.class, NativeImage.class).setAccessible(true);
			return clazz;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("BitmapProvider$ImageDataHolder not found", e);
		}
	}

	private static Field field(Class<?> owner, String name) {
		try {
			Field field = owner.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Field " + owner.getName() + "." + name + " not found", e);
		}
	}

	private static Constructor<?> ctor(Class<?> owner, Class<?>... params) {
		try {
			Constructor<?> ctor = owner.getDeclaredConstructor(params);
			ctor.setAccessible(true);
			return ctor;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Constructor " + owner.getName() + " not found", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Constructor<BitmapProvider.Glyph> glyphCtor() {
		try {
			Constructor<BitmapProvider.Glyph> ctor = BitmapProvider.Glyph.class.getDeclaredConstructor(
					float.class, holderClass(), int.class, int.class, int.class, int.class, int.class, int.class);
			ctor.setAccessible(true);
			return ctor;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("BitmapProvider$Glyph constructor not found", e);
		}
	}

	public static Object getImageData(BitmapProvider provider) {
		try {
			return IMAGE_DATA.get(provider);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to read BitmapProvider.imageData", e);
		}
	}

	public static Identifier getIdentifier(Object holder) {
		try {
			return (Identifier) HOLDER_IDENTIFIER.get(holder);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to read ImageDataHolder.identifier", e);
		}
	}

	public static NativeImage getImage(Object holder) {
		try {
			return (NativeImage) HOLDER_IMAGE.get(holder);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to read ImageDataHolder.image", e);
		}
	}

	public static Object newHolder(Identifier identifier, NativeImage image) {
		try {
			return HOLDER_CTOR.newInstance(identifier, image);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to construct ImageDataHolder", e);
		}
	}

	public static BitmapProvider newProvider(Object holder, CodepointMap<BitmapProvider.Glyph> glyphs) {
		try {
			return (BitmapProvider) PROVIDER_CTOR.newInstance(holder, glyphs);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to construct BitmapProvider", e);
		}
	}

	public static BitmapProvider.Glyph newGlyph(float scale, Object holder, int x, int y, int width, int height, int advance, int ascent) {
		try {
			return GLYPH_CTOR.newInstance(scale, holder, x, y, width, height, advance, ascent);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to construct BitmapProvider.Glyph", e);
		}
	}
}
