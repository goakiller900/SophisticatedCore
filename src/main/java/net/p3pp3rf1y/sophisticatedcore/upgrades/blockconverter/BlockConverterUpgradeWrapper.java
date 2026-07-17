package net.p3pp3rf1y.sophisticatedcore.upgrades.blockconverter;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.SlottedStackStorage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class BlockConverterUpgradeWrapper<U extends BlockConverterUpgradeItem<U, W>, W extends BlockConverterUpgradeWrapper<U, W>>
		extends UpgradeWrapperBase<W, U> {
	private final SlottedStackStorage inputInventory;

	protected BlockConverterUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);

		inputInventory = new ItemStackHandler(1) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				if (slot == 0) {
					ItemStack inputStack = getStackInSlot(0);
					if (inputStack.isEmpty()) {
						upgrade.sophisticatedCore_remove(ModCoreDataComponents.INPUT_ITEM);
					} else {
						upgrade.sophisticatedCore_set(ModCoreDataComponents.INPUT_ITEM, SimpleItemContent.copyOf(inputStack));
					}
				}
				save();
			}
		};
		inputInventory.setStackInSlot(0, upgrade.sophisticatedCore_getOrDefault(ModCoreDataComponents.INPUT_ITEM, SimpleItemContent.EMPTY).copy());
	}

	public SlottedStackStorage getInputInventory() {
		return inputInventory;
	}

	public void setRecipeId(@Nullable ResourceKey<Recipe<?>> recipeId) {
		if (recipeId == null) {
			upgrade.sophisticatedCore_remove(ModCoreDataComponents.RECIPE_ID);
			return;
		}
		upgrade.sophisticatedCore_set(ModCoreDataComponents.RECIPE_ID, recipeId);
		save();
	}

	public Optional<ResourceKey<Recipe<?>>> getRecipeId() {
		return Optional.ofNullable(upgrade.sophisticatedCore_get(ModCoreDataComponents.RECIPE_ID));
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	public boolean shouldShiftClickIntoStorage() {
		return upgrade.sophisticatedCore_getOrDefault(ModCoreDataComponents.SHIFT_CLICK_INTO_STORAGE, true);
	}

	public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
		upgrade.sophisticatedCore_set(ModCoreDataComponents.SHIFT_CLICK_INTO_STORAGE, shiftClickIntoStorage);
		save();
	}

	public boolean shouldRefillInput() {
		return upgrade.sophisticatedCore_getOrDefault(ModCoreDataComponents.REFILL_INPUT, false);
	}

	public void setRefillInput(boolean refillInput) {
		upgrade.sophisticatedCore_set(ModCoreDataComponents.REFILL_INPUT, refillInput);
		save();
	}

	public ItemStack extractFromStorage(ItemStack stack, boolean simulate) {
		return InventoryHelper.extractFromInventory(stack, storageWrapper.getInventoryHandler(), simulate);
	}
}
