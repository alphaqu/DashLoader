package dev.notalpha.dashloader.client.sprite.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import dev.notalpha.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DashSpriteContents implements DashObject<SpriteContents, SpriteContents> {
	private final static Method sodiumScanMethod = getSodiumScanner();
	private final static ResourceMetadataSerializer<?>[] metadataTypes = {
			net.minecraft.client.resource.metadata.GuiResourceMetadata.SERIALIZER,
			net.minecraft.client.resource.metadata.TextureResourceMetadata.SERIALIZER,
			net.minecraft.client.resource.metadata.AnimationResourceMetadata.SERIALIZER,
			net.minecraft.client.render.entity.feature.VillagerResourceMetadata.SERIALIZER,
			net.minecraft.client.resource.metadata.LanguageResourceMetadata.SERIALIZER
	};
	private final static Map<String, ResourceMetadataSerializer<?>> metadataByName = new HashMap<>();

	static {
		for (ResourceMetadataSerializer<?> type : metadataTypes) {
			metadataByName.put(type.name(), type);
		}
	}

	public final int id;
	public final int image;
	@Nullable
	@DataNullable
	public final DashSpriteAnimation animation;
	public final int width;
	public final int height;
	public final String[] metadataNames;
	public final String[] metadataValues;

	public DashSpriteContents(int id, int image, @Nullable DashSpriteAnimation animation, int width, int height,
			String[] metadataNames, String[] metadataValues) {
		this.id = id;
		this.image = image;
		this.animation = animation;
		this.width = width;
		this.height = height;
		this.metadataNames = metadataNames;
		this.metadataValues = metadataValues;
	}

	public DashSpriteContents(SpriteContents contents, RegistryWriter writer) {
		var access = (SpriteContentsAccessor) contents;
		this.id = writer.add(contents.getId());
		this.image = writer.add(access.getImage());
		this.width = contents.getWidth();
		this.height = contents.getHeight();
		SpriteContents.Animation animation = access.getAnimation();
		this.animation = animation == null ? null : new DashSpriteAnimation(animation);

		List<String> names = new ArrayList<>();
		List<String> values = new ArrayList<>();
		ResourceMetadata metadata = access.getMetadata();
		if (metadata != null) {
			for (ResourceMetadataSerializer<?> type : metadataTypes) {
				collectMetadata(metadata, type, names, values);
			}
		}
		this.metadataNames = names.toArray(String[]::new);
		this.metadataValues = values.toArray(String[]::new);
	}

	public SpriteContents export(RegistryReader reader) {
		final SpriteContents out = UnsafeHelper.allocateInstance(SpriteContents.class);
		var access = (SpriteContentsAccessor) out;
		access.setId(reader.get(this.id));

		NativeImage image = reader.get(this.image);
		access.setImage(image);
		access.setHeight(height);
		access.setWidth(width);
		access.setMipmapLevelsImages(new NativeImage[]{image});
		access.setAnimation(this.animation == null ? null : animation.export(out, reader));
		access.setMetadata(importMetadata());
		applySodiumScanning(out, image); // run important sodium method if present
		return out;
	}

	private static <T> void collectMetadata(ResourceMetadata metadata, ResourceMetadataSerializer<T> type,
			List<String> names, List<String> values) {
		metadata.decode(type).ifPresent(value -> encodeMetadata(type, value).ifPresent(json -> {
			names.add(type.name());
			values.add(json);
		}));
	}

	private ResourceMetadata importMetadata() {
		ResourceMetadata.Builder builder = new ResourceMetadata.Builder();
		for (int i = 0; i < metadataNames.length && i < metadataValues.length; i++) {
			ResourceMetadataSerializer<?> type = metadataByName.get(metadataNames[i]);
			if (type == null) continue;
			decodeMetadata(type, metadataValues[i])
					.ifPresent(value -> addMetadata(builder, type, value));
		}
		return builder.build();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void addMetadata(ResourceMetadata.Builder builder, ResourceMetadataSerializer<?> type, Object value) {
		builder.add((ResourceMetadataSerializer) type, value);
	}

	private static Optional<String> encodeMetadata(ResourceMetadataSerializer<?> type, Object value) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		Optional<JsonElement> encoded = ((Codec) type.codec()).encodeStart(JsonOps.INSTANCE, value).result();
		return encoded.map(JsonElement::toString);
	}

	private static Optional<Object> decodeMetadata(ResourceMetadataSerializer<?> type, String json) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		Optional<Object> decoded = ((Codec) type.codec()).parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result();
		return decoded;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashSpriteContents that = (DashSpriteContents) o;

		if (id != that.id) return false;
		if (image != that.image) return false;
		if (width != that.width) return false;
		if (height != that.height) return false;
		if (!Arrays.equals(metadataNames, that.metadataNames)) return false;
		if (!Arrays.equals(metadataValues, that.metadataValues)) return false;
		return Objects.equals(animation, that.animation);
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + image;
		result = 31 * result + (animation != null ? animation.hashCode() : 0);
		result = 31 * result + width;
		result = 31 * result + height;
		result = 31 * result + Arrays.hashCode(metadataNames);
		result = 31 * result + Arrays.hashCode(metadataValues);
		return result;
	}

	private static Method getSodiumScanner() {
		try {
			Method scanSpriteContents = SpriteContents.class.getDeclaredMethod("scanSpriteContents", NativeImage.class);
			scanSpriteContents.setAccessible(true);
			return scanSpriteContents;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private void applySodiumScanning(SpriteContents contents, NativeImage image) {
		if (sodiumScanMethod == null) return;
		try {
			sodiumScanMethod.invoke(contents, image);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
