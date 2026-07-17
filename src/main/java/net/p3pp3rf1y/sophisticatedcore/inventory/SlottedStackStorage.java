package net.p3pp3rf1y.sophisticatedcore.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public interface SlottedStackStorage extends SlottedStorage<ItemVariant> {
	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	default boolean isItemValid(int slot, ItemVariant resource, int count) {
		return true;
	}

	default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).insert(resource, maxAmount, transaction);
	}

	default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getSlot(slot).extract(resource, maxAmount, transaction);
	}

	@Override
	default Iterator<StorageView<ItemVariant>> iterator() {
		//noinspection unchecked,rawtypes
		return (Iterator) getSlots().iterator();
	}
}
