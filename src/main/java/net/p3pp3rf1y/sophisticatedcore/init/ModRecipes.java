package net.p3pp3rf1y.sophisticatedcore.init;

import com.mojang.serialization.MapCodec;
import net.p3pp3rf1y.sophisticatedcore.util.DeferredRegister;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.crafting.ItemEnabledCondition;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;

import java.util.function.Supplier;

public class ModRecipes {
	private ModRecipes() {
	}

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedCore.MOD_ID);


	public static final Supplier<RecipeSerializer<?>> UPGRADE_NEXT_TIER_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_next_tier", () -> UpgradeNextTierRecipe.SERIALIZER);
	private static final UpgradeClearRecipe UPGRADE_CLEAR_RECIPE = new UpgradeClearRecipe();
	public static final Supplier<RecipeSerializer<UpgradeClearRecipe>> UPGRADE_CLEAR_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade_clear",
			() -> new RecipeSerializer<>(MapCodec.unit(UPGRADE_CLEAR_RECIPE), StreamCodec.<RegistryFriendlyByteBuf, UpgradeClearRecipe>unit(UPGRADE_CLEAR_RECIPE)));

	public static final ResourceConditionType<ItemEnabledCondition> ITEM_ENABLED_CONDITION = ResourceConditionType.create(SophisticatedCore.getIdentifier("item_enabled"), ItemEnabledCondition.CODEC);

	public static void registerHandlers() {
		RECIPE_SERIALIZERS.register();

		ResourceConditions.register(ITEM_ENABLED_CONDITION);
	}
}
