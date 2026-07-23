package net.p3pp3rf1y.sophisticatedcore.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.FluidState;
import net.p3pp3rf1y.sophisticatedcore.util.TransactionCallback;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class BucketPickupHandlerWrapper implements SingleSlotStorage<FluidVariant>, ExtractionOnlyStorage<FluidVariant> {
	private final BucketPickup bucketPickup;
	private final Level level;
	private final BlockPos pos;
	/**
	 * StorageUtil.move performs a nested simulation extract before the real transfer extract.
	 * We only want to mutate the world once, on the real transfer pass.
	 */
	private final Set<TransactionContext> pendingOuterTransactions = Collections.newSetFromMap(new WeakHashMap<>());

	public BucketPickupHandlerWrapper(BucketPickup bucketPickup, Level level, BlockPos pos) {
		this.bucketPickup = bucketPickup;
		this.level = level;
		this.pos = pos;
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		if (!resource.isBlank() && maxAmount >= FluidConstants.BUCKET) {
			FluidState state = level.getFluidState(pos);
			if (!state.isEmpty() && resource.equals(FluidVariant.of(state.getType()))) {
				if (!pendingOuterTransactions.add(transaction)) {
					BlockState capturedState = level.getBlockState(pos);
					TransactionCallback.onSuccess(transaction, () -> bucketPickup.pickupBlock(null, level, pos, capturedState));
					pendingOuterTransactions.remove(transaction);
				}
				return FluidConstants.BUCKET;
			}
		}
		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		return FluidVariant.of(level.getFluidState(pos).getType());
	}

	@Override
	public long getAmount() {
		return level.getFluidState(pos).isEmpty() ? 0 : FluidConstants.BUCKET;
	}

	@Override
	public long getCapacity() {
		return FluidConstants.BUCKET;
	}
}
