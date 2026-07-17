package net.p3pp3rf1y.sophisticatedcore.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;

public record ItemEnabledCondition(Identifier itemRegistryName) implements ResourceCondition {
	public static final MapCodec<ItemEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
			builder -> builder
					.group(
							Identifier.CODEC.fieldOf("itemRegistryName").forGetter(ItemEnabledCondition::itemRegistryName))
					.apply(builder, ItemEnabledCondition::new));

	public ItemEnabledCondition(Item item) {
		this(BuiltInRegistries.ITEM.getKey(item));
	}

	@Override
	public boolean test(RegistryOps.RegistryInfoLookup registryLookup) {
		return Config.COMMON.enabledItems.isItemEnabled(itemRegistryName);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return ModRecipes.ITEM_ENABLED_CONDITION;
	}
}
