package net.p3pp3rf1y.sophisticatedcore.util;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuTypeExtension<T> {
	static <T extends AbstractContainerMenu> MenuType<T> create(MenuFactory<T> factory) {
		return new ExtendedMenuType<>((windowId, inventory, data) -> {
			RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
			T menu = factory.create(windowId, inventory, buf);
			buf.release();
			return menu;
		}, ByteBufCodecs.BYTE_ARRAY);
	}

	@FunctionalInterface
	interface MenuFactory<T extends AbstractContainerMenu> {
		T create(int windowId, net.minecraft.world.entity.player.Inventory inventory, RegistryFriendlyByteBuf data);
	}
}
