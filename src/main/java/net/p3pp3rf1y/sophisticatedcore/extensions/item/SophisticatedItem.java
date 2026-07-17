package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import javax.annotation.Nullable;

public interface SophisticatedItem {
	default float getXpRepairRatio(ItemStack stack) {
		return 1.0F;
	}

	default boolean onDroppedByPlayer(ItemStack stack, Player player) {
		return true;
	}

	@OverrideOnly
	default int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return 0;
	}

    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        return InteractionResult.PASS;
    }

	default @Nullable FoodProperties getFoodProperties(ItemStack stack,  @Nullable LivingEntity entity) {
		return stack.get(DataComponents.FOOD);
	}

	@OverrideOnly
	default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
		return stack.getEnchantments().getLevel(enchantment);
	}

	default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !oldStack.equals(newStack);
	}

	default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.assetId().filter(EquipmentAssets.GOLD::equals).isPresent();
	}
}
