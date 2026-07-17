package net.p3pp3rf1y.sophisticatedcore.util;

import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityTypes;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.inventory.IInventoryHandlerHelper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryStorageWrapper;

import java.util.List;

public class Capabilities {
	public static class ItemHandler {
		/**
		 * Capability for the inventory of an entity.
		 * If an entity has multiple inventory "subparts", this capability should give a combined view of all the subparts.
		 */
		public static final EntityApiLookup<IInventoryHandlerHelper, Void> ENTITY = EntityApiLookup.get(SophisticatedCore.getIdentifier("entity_item_storage"), IInventoryHandlerHelper.class, Void.class);

		/**
		 * Capability for an inventory of entity that should be accessible to automation,
		 * in the sense that droppers, hoppers, and similar modded devices will try to use it.
		 */
		public static final EntityApiLookup<IInventoryHandlerHelper, Direction> ENTITY_AUTOMATION = EntityApiLookup.get(SophisticatedCore.getIdentifier("entity_automation_item_storage"), IInventoryHandlerHelper.class, Direction.class);

		static {
			var containerEntities = List.of(
					EntityTypes.ACACIA_CHEST_BOAT, EntityTypes.BIRCH_CHEST_BOAT, EntityTypes.CHERRY_CHEST_BOAT,
					EntityTypes.DARK_OAK_CHEST_BOAT, EntityTypes.JUNGLE_CHEST_BOAT, EntityTypes.MANGROVE_CHEST_BOAT,
					EntityTypes.OAK_CHEST_BOAT, EntityTypes.PALE_OAK_CHEST_BOAT, EntityTypes.SPRUCE_CHEST_BOAT,
					EntityTypes.CHEST_MINECART, EntityTypes.HOPPER_MINECART);
			for (var entityType : containerEntities) {
				ENTITY.registerForType((entity, ctx) -> InventoryStorageWrapper.of((Container) entity), entityType);
				ENTITY_AUTOMATION.registerForType((entity, direction) -> InventoryStorageWrapper.of((Container) entity), entityType);
			}

			ENTITY.registerForType((player, ctx) -> InventoryStorageWrapper.of(player), EntityTypes.PLAYER);
		}
	}
}
