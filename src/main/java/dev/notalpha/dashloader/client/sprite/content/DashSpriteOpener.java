package dev.notalpha.dashloader.client.sprite.content;

import dev.notalpha.dashloader.mixin.accessor.SpriteContentsAccessor;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DashSpriteOpener implements SpriteOpener {
	private final SpriteOpener inner;
	private final Collection<ResourceMetadataReader<?>> metadatas;
	public final DashAtlasData data;

	public DashSpriteOpener(SpriteOpener inner, Collection<ResourceMetadataReader<?>> metadatas, DashAtlasData data) {
		this.inner = inner;
		this.metadatas = metadatas;
		this.data = data;
	}

	@Nullable
	@Override
	public SpriteContents loadSprite(Identifier id, Resource resource) {
		SpriteContents existingContents = data.sprites.get(id);
		if (existingContents != null) {
			ResourceMetadata resourceMetadata;
			try {
				resourceMetadata = resource.getMetadata().copy(metadatas);
			} catch (Exception var9) {
				LOGGER.error("Unable to parse metadata from {}", id, var9);
				return null;
			}
			((SpriteContentsAccessor) existingContents).setMetadata(resourceMetadata);
			return existingContents;
		}

		SpriteContents spriteContents = inner.loadSprite(id, resource);
		data.sprites.put(id, spriteContents);
		return spriteContents;
	}
}
