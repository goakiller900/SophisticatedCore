package net.p3pp3rf1y.sophisticatedcore.crafting;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StorageDyeRecipeBase extends CustomRecipe {
	protected StorageDyeRecipeBase(CraftingBookCategory category) {
	}

	@Override
	public boolean matches(CraftingInput inv, Level worldIn) {
		boolean storagePresent = false;
		boolean dyePresent = false;
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.isEmpty()) {
				continue;
			}
			if (isDyeableStorageItem(slotStack)) {
				if (storagePresent) {
					return false;
				}
				storagePresent = true;
			} else if (slotStack.is(ConventionalItemTags.DYES)) {
				dyePresent = true;
			} else {
				return false;
			}
		}
		return storagePresent && dyePresent;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		Map<Integer, List<DyeColor>> columnDyes = new HashMap<>();
		Pair<Integer, ItemStack> columnStorage = null;

		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.isEmpty()) {
				continue;
			}
			int column = slot % inv.width();
			if (isDyeableStorageItem(slotStack)) {
				if (columnStorage != null) {
					return ItemStack.EMPTY;
				}

				columnStorage = Pair.of(column, slotStack);
			} else if (slotStack.is(ConventionalItemTags.DYES)) {
				DyeColor dyeColor = getColorFromStack(slotStack);
				if (dyeColor == null) {
					return ItemStack.EMPTY;
				}
				columnDyes.computeIfAbsent(column, c -> new ArrayList<>()).add(dyeColor);
			} else {
				return ItemStack.EMPTY;
			}
		}
		if (columnStorage == null) {
			return ItemStack.EMPTY;
		}

		ItemStack coloredStorage = columnStorage.getSecond().copy();
		coloredStorage.setCount(1);
		int storageColumn = columnStorage.getFirst();

		applyTintColors(columnDyes, coloredStorage, storageColumn);

		return coloredStorage;
	}

	protected abstract boolean isDyeableStorageItem(ItemStack stack);

	private void applyTintColors(Map<Integer, List<DyeColor>> columnDyes, ItemStack coloredStorage, int storageColumn) {
		List<DyeColor> mainDyes = new ArrayList<>();
		List<DyeColor> trimDyes = new ArrayList<>();

		for (Map.Entry<Integer, List<DyeColor>> entry : columnDyes.entrySet()) {
			if (entry.getKey() <= storageColumn) {
				mainDyes.addAll(entry.getValue());
			}
			if (entry.getKey() >= storageColumn) {
				trimDyes.addAll(entry.getValue());
			}
		}

		applyColors(coloredStorage, mainDyes, trimDyes);
	}

	protected abstract void applyColors(ItemStack coloredStorage, List<DyeColor> mainDyes, List<DyeColor> trimDyes);

	public boolean canCraftInDimensions(int width, int height) {
		return width >= 2 && height >= 1;
	}

	@Nullable
	public static DyeColor getColorFromStack(ItemStack stack) {
		for (DyeColor color : DyeColor.values()) {
			if (stack.is(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", color.getName() + "_dyes"))))
				return color;
		}

		return null;
	}
}
