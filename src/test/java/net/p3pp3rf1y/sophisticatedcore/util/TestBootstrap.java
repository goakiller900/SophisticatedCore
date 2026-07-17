package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;

final class TestBootstrap {
	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		DataComponentMap defaultStackableItemComponents = DataComponentMap.builder()
				.set(DataComponents.MAX_STACK_SIZE, 64)
				.build();
		DataComponentMap defaultUnstackableItemComponents = DataComponentMap.builder()
				.set(DataComponents.MAX_STACK_SIZE, 1)
				.build();
		BuiltInRegistries.ITEM.listElements()
				.filter(holder -> !holder.areComponentsBound())
				.forEach(holder -> holder.bindComponents(holder.value() == Items.IRON_SWORD ? defaultUnstackableItemComponents : defaultStackableItemComponents));
	}

	private TestBootstrap() {
	}

	static void initialize() {
		// Triggers the static initializer once for all unit tests in this JVM.
	}
}
