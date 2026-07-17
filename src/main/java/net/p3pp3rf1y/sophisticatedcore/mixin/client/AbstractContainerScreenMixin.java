package net.p3pp3rf1y.sophisticatedcore.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory.SophisticatedAbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin implements SophisticatedAbstractContainerScreen {
	@Shadow
	protected int imageWidth;
	@Shadow
	protected int leftPos;
	@Shadow
	protected int topPos;
	@Shadow
	@Nullable
	protected Slot hoveredSlot;

	@Override
	public int sophisticatedCore_getXSize() {
		return imageWidth;
	}

	@Override
	public int sophisticatedCore_getGuiLeft() {
		return leftPos;
	}

	@Override
	public int sophisticatedCore_getGuiTop() {
		return topPos;
	}

	@Override
	@Nullable
	public Slot sophisticatedCore_getSlotUnderMouse() {
		return hoveredSlot;
	}
}
