package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.InteractionResult;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRawInputEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;overlay()Lnet/minecraft/client/gui/screens/Overlay;"), cancellable = true)
    private void sophisticatedCore$onScroll(long handle, double xOffset, double yOffset, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
			boolean discreteScroll = minecraft.options.discreteMouseScroll().get();
			double sensitivity = minecraft.options.mouseWheelSensitivity().get();
			double deltaX = (discreteScroll ? Math.signum(xOffset) : xOffset) * sensitivity;
			double deltaY = (discreteScroll ? Math.signum(yOffset) : yOffset) * sensitivity;
            var result = ClientRawInputEvent.MOUSE_SCROLLED.invoker().mouseScrolled(minecraft, deltaX, deltaY);
            if (result != InteractionResult.PASS)
                ci.cancel();
        }
    }
}
