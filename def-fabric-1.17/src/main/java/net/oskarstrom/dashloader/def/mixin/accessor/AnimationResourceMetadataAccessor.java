package net.oskarstrom.dashloader.def.mixin.accessor;

import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AnimationResourceMetadata.class)
public interface AnimationResourceMetadataAccessor {

	@Accessor
	List<AnimationFrameResourceMetadata> getFrames();

	@Accessor
	int getWidth();

	@Accessor
	int getHeight();

	@Accessor
	int getDefaultFrameTime();

	@Accessor
	boolean getInterpolate();
}
