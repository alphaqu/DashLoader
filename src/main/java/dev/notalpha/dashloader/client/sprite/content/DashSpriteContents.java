package dev.notalpha.dashloader.client.sprite.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.texture.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import dev.notalpha.hyphen.scan.annotations.DataNullable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.render.entity.feature.VillagerResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.resource.metadata.GuiResourceMetadata;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public final class DashSpriteContents implements DashObject<SpriteContents, SpriteContents> {
	private final static Method sodiumScanMethod = getSodiumScanner();
	private final static ResourceMetadataSerializer<?>[] metadataTypes = {
			GuiResourceMetadata.SERIALIZER,
			TextureResourceMetadata.SERIALIZER,
			AnimationResourceMetadata.SERIALIZER,
			VillagerResourceMetadata.SERIALIZER,
			LanguageResourceMetadata.SERIALIZER
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
		for (ResourceMetadataSerializer.Value<?> withValue : access.getAdditionalMetadata()) {
			ResourceMetadataSerializer<?> type = withValue.type();
			Optional<String> json = encodeMetadata(type, withValue.value());
			if (json.isPresent()) {
				names.add(type.name());
				values.add(json.get());
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
		access.setAdditionalMetadata(importMetadata());
		applySodiumScanning(out, image); // run important sodium method if present
		return out;
	}

	private List<ResourceMetadataSerializer.Value<?>> importMetadata() {
		List<ResourceMetadataSerializer.Value<?>> out = new ArrayList<>(metadataNames.length);
		for (int i = 0; i < metadataNames.length && i < metadataValues.length; i++) {
			ResourceMetadataSerializer<?> type = metadataByName.get(metadataNames[i]);
			if (type == null) continue;
			decodeMetadata(type, metadataValues[i])
					.ifPresent(value -> out.add(withValue(type, value)));
		}
		return out;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static ResourceMetadataSerializer.Value<?> withValue(ResourceMetadataSerializer<?> type, Object value) {
		return new ResourceMetadataSerializer.Value(type, value);
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
