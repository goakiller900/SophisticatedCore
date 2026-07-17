package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.extensions.inventory.SophisticatedSlot;
import net.p3pp3rf1y.sophisticatedcore.util.MixinHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin implements SophisticatedSlot {
    @Unique
    private Identifier sophisticatedCore_background;

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void sophisticatedcore$background(CallbackInfoReturnable<Identifier> cir) {
        if (sophisticatedCore_background != null) {
            cir.setReturnValue(sophisticatedCore_background);
        }
    }

    @Override
    public Slot sophisticatedCore_setBackground(Identifier atlas, Identifier sprite) {
		this.sophisticatedCore_background = sprite;
        return MixinHelper.cast(this);
    }

    @Unique
    @Override
    public int sophisticatedCore_getSlotIndex() {
		return MixinHelper.<Slot>cast(this).getContainerSlot();
    }
}
