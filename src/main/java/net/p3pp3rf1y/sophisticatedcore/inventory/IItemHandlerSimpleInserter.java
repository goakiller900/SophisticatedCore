package net.p3pp3rf1y.sophisticatedcore.inventory;

import net.p3pp3rf1y.sophisticatedcore.inventory.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public interface IItemHandlerSimpleInserter extends SlottedStackStorage, IInventoryHandlerHelper {
	default boolean isItemValid(int slot, ItemStack stack) {
		return isItemValid(slot, ItemVariant.of(stack), stack.getCount());
	}

	@Override
	/// Do not override, override {@link #isItemValid(int, ItemStack)} instead
	default boolean isItemValid(int slot, ItemVariant resource, int count) {
		return isItemValid(slot, resource.toStack(count));
	}
}
