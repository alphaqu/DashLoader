package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.mixin.accessor.SpriteContentsAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.DashUtil;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import org.jetbrains.annotations.Nullable;

public final class DashSpriteContents {
    public final int id;
    public final int image;
    @Nullable
    @DataNullable
    public final DashSpriteAnimation animation;

    public final int width;
    public final int height;
    public final int mipMaps;

    public DashSpriteContents(int id, int image, @Nullable DashSpriteAnimation animation, int width, int height, int mipMaps) {
        this.id = id;
        this.image = image;
        this.animation = animation;
        this.width = width;
        this.height = height;
        this.mipMaps = mipMaps;
    }

    public DashSpriteContents(SpriteContents contents, RegistryWriter writer) {

        var access = (SpriteContentsAccessor) contents;
        this.id = writer.add(contents.getId());
        this.image = writer.add(access.getImage());
        this.width = contents.getWidth();
        this.height = contents.getHeight();
        this.mipMaps = access.getMipmapLevelsImages().length - 1;
        this.animation = DashUtil.nullable(access.getAnimation(), DashSpriteAnimation::new);
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
        out.generateMipmaps(this.mipMaps);
        access.setAnimation(DashUtil.nullable(this.animation, animation -> animation.export(out, reader)));
        return out;
    }
}
