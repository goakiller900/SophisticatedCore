package net.p3pp3rf1y.sophisticatedcore.upgrades;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;

import java.util.List;
import java.util.function.Consumer;

public abstract class UpgradeItemBase<T extends IUpgradeWrapper> extends ItemBase implements IUpgradeItem<T> {
	private final IUpgradeCountLimitConfig upgradeTypeLimitConfig;

	protected UpgradeItemBase(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		this(upgradeTypeLimitConfig, new Properties());
	}

	protected UpgradeItemBase(IUpgradeCountLimitConfig upgradeTypeLimitConfig, Properties properties) {
		super(properties);
		this.upgradeTypeLimitConfig = upgradeTypeLimitConfig;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flagIn) {
		TranslationHelper.INSTANCE.getTranslatedLines(stack.getItem().getDescriptionId() + TranslationHelper.TOOLTIP_SUFFIX, null, ChatFormatting.DARK_GRAY)
				.forEach(tooltipAdder);
	}

	@Override
	public int getUpgradesPerStorage(String storageType) {
		return upgradeTypeLimitConfig.getMaxUpgradesPerStorage(storageType, BuiltInRegistries.ITEM.getKey(this));
	}

	@Override
	public int getUpgradesInGroupPerStorage(String storageType) {
		if (getUpgradeGroup().isSolo()) {
			return Integer.MAX_VALUE;
		}

		return upgradeTypeLimitConfig.getMaxUpgradesInGroupPerStorage(storageType, getUpgradeGroup());
	}

	@Override
	public Component getName() {
		return Component.translatable(getDescriptionId());
	}
}
