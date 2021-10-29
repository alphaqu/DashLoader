package dev.quantumfusion.dashloader.def.mixin.feature.misc;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import dev.quantumfusion.dashloader.def.DashLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {


	protected TitleScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE",
					target = "Ljava/util/List;iterator()Ljava/util/Iterator;"),
			cancellable = true)
	private void waterMark(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		DrawableHelper.drawStringWithShadow(matrices, this.textRenderer, "DashLoader (" + DashLoader.VERSION + ")", 2, this.height - 12 - textRenderer.fontHeight, 16777215);
	}
}
