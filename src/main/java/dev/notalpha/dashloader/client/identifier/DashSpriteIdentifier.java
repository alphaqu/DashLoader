package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import net.minecraft.client.resources.model.sprite.SpriteId;

public class DashSpriteIdentifier implements DashObject<SpriteId, SpriteId> {
	public final int atlas;
	public final int texture;

	public DashSpriteIdentifier(int atlas, int texture) {
		this.atlas = atlas;
		this.texture = texture;
	}

	public DashSpriteIdentifier(SpriteId identifier, RegistryWriter writer) {
		this.atlas = writer.add(identifier.atlasLocation());
		this.texture = writer.add(identifier.texture());
	}

	@Override
	public SpriteId export(RegistryReader reader) {
		return new SpriteId(reader.get(atlas), reader.get(texture));
	}
}
