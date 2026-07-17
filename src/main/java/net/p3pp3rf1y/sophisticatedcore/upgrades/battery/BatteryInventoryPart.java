package net.p3pp3rf1y.sophisticatedcore.upgrades.battery;

import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

/**
 * @deprecated Use {@link BatteryInventoryControl}. Kept for binary-compatible factory registrations.
 */
@Deprecated(forRemoval = false)
public class BatteryInventoryPart extends BatteryInventoryControl {
	public BatteryInventoryPart(int upgradeSlot, BatteryUpgradeContainer container, Position pos, int height, StorageScreenBase<?> screen) {
		super(upgradeSlot, container, pos, height, screen);
	}
}
