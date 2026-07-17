package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.p3pp3rf1y.sophisticatedcore.event.client.ClientLifecycleEvents;
import net.p3pp3rf1y.sophisticatedcore.event.common.EntityEvents;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void sophisticatedcore$construct(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, int i, int j, LevelExtractor levelExtractor, boolean bl, long l, int seaLevel, CallbackInfo ci) {
        ClientLifecycleEvents.CLIENT_LEVEL_LOAD.invoker().onWorldLoad(minecraft, MixinHelper.cast(this));
    }

	@WrapOperation(method = "addDestroyBlockEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;shouldSpawnTerrainParticles()Z"))
	private boolean sophisticatedcore$addDestroyEffects(BlockState blockState, Operation<Boolean> original, BlockPos pos, BlockState state) {
		return !blockState.sophisticatedCore_addDestroyEffects(MixinHelper.cast(this), pos, minecraft.particleEngine)
				&& original.call(blockState);
	}

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void sophisticatedcore$addEntityEvent(Entity entity, CallbackInfo ci) {
        if (EntityEvents.ON_JOIN_WORLD.invoker().onJoinWorld(entity, MixinHelper.cast(this), false))
            ci.cancel();
    }
}
