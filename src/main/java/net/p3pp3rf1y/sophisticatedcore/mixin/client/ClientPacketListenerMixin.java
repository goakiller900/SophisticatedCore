package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientRecipesUpdated;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
	private void sophisticatedCore$handleUpdateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
		ClientRecipesUpdated.EVENT.invoker().onRecipesUpdated();
	}
}
