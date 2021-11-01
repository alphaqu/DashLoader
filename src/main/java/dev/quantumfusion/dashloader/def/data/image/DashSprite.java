package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@DashObject(Sprite.class)
public interface DashSprite extends Dashable<Sprite> {
}
