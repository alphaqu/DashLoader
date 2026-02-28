package dev.notalpha.dashloader.client.identifier;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import net.minecraft.client.resources.model.Material;

public class DashSpriteIdentifier implements DashObject<Material, Material> {
	public final int atlas;
	public final int texture;

	public DashSpriteIdentifier(int atlas, int texture) {
		this.atlas = atlas;
		this.texture = texture;
	}

	public DashSpriteIdentifier(Material identifier, RegistryWriter writer) {
		this.atlas = writer.add(identifier.atlasLocation());
		this.texture = writer.add(identifier.texture());
	}

	@Override
	public Material export(RegistryReader reader) {
		return new Material(reader.get(atlas), reader.get(texture));
	}
}
