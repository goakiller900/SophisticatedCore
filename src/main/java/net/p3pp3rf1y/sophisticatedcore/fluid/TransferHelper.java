package net.p3pp3rf1y.sophisticatedcore.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public final class TransferHelper {
	private TransferHelper() {
	}

	public static FluidStack simulateExtractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
		try (Transaction transaction = Transaction.openOuter()) {
			ResourceAmount<FluidVariant> extracted = StorageUtil.extractAny(storage, maxAmount, transaction);
			return extracted == null ? FluidStack.EMPTY : new FluidStack(extracted);
		}
	}

	public static FluidStack getFirstFluid(Storage<FluidVariant> storage) {
		for (var view : storage.nonEmptyViews()) {
			return new FluidStack(view);
		}
		return FluidStack.EMPTY;
	}
}
