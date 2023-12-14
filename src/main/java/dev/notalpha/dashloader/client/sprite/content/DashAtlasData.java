package dev.notalpha.dashloader.client.sprite.content;

import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;

import java.util.Map;

public class DashAtlasData {
	public final Identifier atlasId;
	public final Map<Identifier, SpriteContents> sprites;

	public DashAtlasData(Identifier atlasId, Map<Identifier, SpriteContents> sprites) {
		this.atlasId = atlasId;
		this.sprites = sprites;
	}
}
