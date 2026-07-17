package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.p3pp3rf1y.sophisticatedcore.extensions.entity.SophisticatedEntity;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Entity.class)
public class EntityMixin implements SophisticatedEntity {
	@Unique
	private static final String SOPHISTICATEDCOREDATA_NBT_KEY = "SophisticatedCoreData";

    @Shadow
	private Level level;

	@Unique
	private Collection<ItemEntity> sophisticatedCore$captureDrops = null;

	@WrapWithCondition(
			method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
			)
	)
	private boolean sophisticatedCore$captureDrops(ServerLevel level, Entity entity) {
		if (sophisticatedCaptureDrops() != null && entity instanceof ItemEntity item) {
			sophisticatedCaptureDrops().add(item);
			return false;
		}
		return true;
	}

	@Unique
	@Override
	public Collection<ItemEntity> sophisticatedCaptureDrops() {
		return this.sophisticatedCore$captureDrops;
	}

	@Unique
	@Override
	public Collection<ItemEntity> sophisticatedCaptureDrops(Collection<ItemEntity> value) {
		Collection<ItemEntity> ret = this.sophisticatedCore$captureDrops;
		this.sophisticatedCore$captureDrops = value;
		return ret;
	}

	@Unique
	private CompoundTag sophisticatedCore$customData;

    @Inject(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"), cancellable = true)
    private void sophisticatedCore$addRunningEffects(CallbackInfo ci, @Local BlockPos blockPos, @Local BlockState blockState) {
        if (blockState.sophisticatedCore_addRunningEffects(level, blockPos, MixinHelper.cast(this))) {
            ci.cancel();
        }
    }

	@Override
	public CompoundTag getSophisticatedCustomData() {
		if (this.sophisticatedCore$customData == null) {
			this.sophisticatedCore$customData = new CompoundTag();
		}
		return this.sophisticatedCore$customData;
	}

	@Inject(method = "saveWithoutId", at = @At("HEAD"))
	public void sophisticatedCore$saveAdditionalData(ValueOutput output, CallbackInfo ci) {
		if (this.sophisticatedCore$customData != null && !this.sophisticatedCore$customData.isEmpty()) {
			output.store(SOPHISTICATEDCOREDATA_NBT_KEY, CompoundTag.CODEC, this.sophisticatedCore$customData);
		}
	}

	@Inject(method = "load", at = @At("HEAD"))
	public void sophisticatedCore$readAdditionalData(ValueInput input, CallbackInfo ci) {
		this.sophisticatedCore$customData = input.read(SOPHISTICATEDCOREDATA_NBT_KEY, CompoundTag.CODEC).orElse(null);
	}
}
