package net.p3pp3rf1y.sophisticatedcore.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;

public class ItemStackHandlerSlot extends SingleStackStorage {
	private final int index;
	private final ItemStackHandler handler;
	private ItemStack stack;
	private ItemStack lastStack;
	private ItemVariant variant;

	public ItemStackHandlerSlot(int index, ItemStackHandler handler, ItemStack initial) {
		this.index = index;
		this.handler = handler;
		lastStack = initial.copy();
		setStack(initial);
		handler.initSlot(this);
	}

	@Override
	protected boolean canInsert(ItemVariant itemVariant) {
		return handler.isItemValid(index, itemVariant, 1);
	}

	@Override
	protected int getCapacity(ItemVariant itemVariant) {
		return handler.getStackLimit(index, itemVariant);
	}

	@Override
	protected ItemStack getStack() {
		return stack;
	}

	@Override
	protected void setStack(ItemStack stack) {
		this.stack = stack;
		variant = ItemVariant.of(stack);
	}

	public void setNewStack(ItemStack stack) {
		setStack(stack);
		onFinalCommit();
	}

	/**
	 * Resets a reused slot while its handler is rebuilding the slot list. Rebuilding is
	 * initialization, not an inventory mutation, so it must not fire a contents-change
	 * callback before the slot has been put back into that list.
	 */
	protected void resetStack(ItemStack stack) {
		setStack(stack);
		lastStack = stack.copy();
		handler.initSlot(this);
	}

	@Override
	public ItemVariant getResource() {
		return variant;
	}

	public int getIndex() {
		return index;
	}

	@Override
	protected void onFinalCommit() {
		handler.onStackChange(this, lastStack, stack);
		lastStack = stack.copy();
		handler.onContentsChanged(index);
	}

	public Tag save(HolderLookup.Provider provider, CompoundTag tag) {
		return CodecHelper.OVERSIZED_ITEM_STACK_CODEC.encode(stack, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
	}

	public void load(HolderLookup.Provider provider, CompoundTag tag) {
		CodecHelper.OVERSIZED_ITEM_STACK_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(error -> SophisticatedCore.LOGGER.error("Could not load inventory stack: {}", error))
				.ifPresent(this::setStack);
		handler.onStackChange(this, lastStack, stack);
		lastStack = stack.copy();
	}
}
