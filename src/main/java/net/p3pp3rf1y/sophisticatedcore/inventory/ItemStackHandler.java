package net.p3pp3rf1y.sophisticatedcore.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;

import java.util.*;

/** Native Fabric transfer storage used by Sophisticated Core inventories. */
public class ItemStackHandler implements SlottedStackStorage {
	private final List<ItemStackHandlerSlot> slots;
	private final SortedSet<ItemStackHandlerSlot> nonEmptySlots;
	private final Map<Item, SortedSet<ItemStackHandlerSlot>> lookup;

	public ItemStackHandler() {
		this(1);
	}

	public ItemStackHandler(int size) {
		this(createEmptyStacks(size));
	}

	public ItemStackHandler(ItemStack[] stacks) {
		slots = new ArrayList<>(stacks.length);
		nonEmptySlots = createSlotSet();
		lookup = new HashMap<>();
		for (int i = 0; i < stacks.length; i++) {
			slots.add(makeSlot(i, stacks[i]));
		}
	}

	private static ItemStack[] createEmptyStacks(int size) {
		ItemStack[] stacks = new ItemStack[size];
		Arrays.fill(stacks, ItemStack.EMPTY);
		return stacks;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long inserted = 0;
		for (Iterator<ItemStackHandlerSlot> iterator = getInsertableSlotsFor(resource); iterator.hasNext() && inserted < maxAmount; ) {
			inserted += iterator.next().insert(resource, maxAmount - inserted, transaction);
		}
		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long extracted = 0;
		for (ItemStackHandlerSlot slot : new ArrayList<>(getSlotsContaining(resource.getItem()))) {
			extracted += slot.extract(resource, maxAmount - extracted, transaction);
			if (extracted >= maxAmount) {
				break;
			}
		}
		return extracted;
	}

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		//noinspection unchecked,rawtypes
		return (Iterable) new ArrayList<>(nonEmptySlots);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		//noinspection unchecked,rawtypes
		return (Iterator) new ArrayList<>(nonEmptySlots).iterator();
	}

	@Override
	public int getSlotCount() {
		return slots.size();
	}

	@Override
	public ItemStackHandlerSlot getSlot(int slot) {
		return slots.get(slot);
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		//noinspection unchecked,rawtypes
		return (List) slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getSlot(slot).getStack();
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		getSlot(slot).setNewStack(stack);
	}

	public ItemVariant getVariantInSlot(int slot) {
		return getSlot(slot).getResource();
	}

	@Override
	public int getSlotLimit(int slot) {
		return getStackInSlot(slot).getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
	}

	protected int getStackLimit(int slot, ItemVariant resource) {
		return Math.min(getSlotLimit(slot), resource.getItem().getDefaultMaxStackSize());
	}

	protected void onContentsChanged(int slot) {
	}

	public SortedSet<ItemStackHandlerSlot> getSlotsContaining(Item item) {
		return lookup.getOrDefault(item, Collections.emptySortedSet());
	}

	protected void onLoad() {
	}

	public boolean empty() {
		return nonEmptySlots.isEmpty();
	}

	public void setSize(int size) {
		slots.clear();
		nonEmptySlots.clear();
		lookup.clear();
		for (int i = 0; i < size; i++) {
			slots.add(makeSlot(i, ItemStack.EMPTY));
		}
	}

	protected ItemStackHandlerSlot makeSlot(int index, ItemStack stack) {
		return new ItemStackHandlerSlot(index, this, stack);
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Size", slots.size());
		ListTag itemTags = new ListTag();
		for (ItemStackHandlerSlot slot : slots) {
			if (!slot.getStack().isEmpty()) {
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("Slot", slot.getIndex());
				itemTags.add(slot.save(provider, itemTag));
			}
		}
		nbt.put("Items", itemTags);
		return nbt;
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setSize(nbt.getIntOr("Size", slots.size()));
		ListTag itemTags = nbt.getListOrEmpty("Items");
		for (int i = 0; i < itemTags.size(); i++) {
			CompoundTag itemTag = itemTags.getCompound(i).orElseGet(CompoundTag::new);
			int index = itemTag.getIntOr("Slot", -1);
			if (index >= 0 && index < slots.size()) {
				slots.get(index).load(provider, itemTag);
			}
		}
		onLoad();
	}

	void onStackChange(ItemStackHandlerSlot slot, ItemStack oldStack, ItemStack newStack) {
		if (ItemStack.isSameItem(oldStack, newStack)) {
			return;
		}
		SortedSet<ItemStackHandlerSlot> oldItemSlots = lookup.get(oldStack.getItem());
		if (oldItemSlots != null) {
			oldItemSlots.remove(slot);
		}
		lookup.computeIfAbsent(newStack.getItem(), item -> createSlotSet()).add(slot);
		if (oldStack.isEmpty() && !newStack.isEmpty()) {
			nonEmptySlots.add(slot);
		} else if (!oldStack.isEmpty() && newStack.isEmpty()) {
			nonEmptySlots.remove(slot);
		}
	}

	void initSlot(ItemStackHandlerSlot slot) {
		ItemStack stack = slot.getStack();
		lookup.computeIfAbsent(stack.getItem(), item -> createSlotSet()).add(slot);
		if (!stack.isEmpty()) {
			nonEmptySlots.add(slot);
		}
	}

	private Iterator<ItemStackHandlerSlot> getInsertableSlotsFor(ItemVariant variant) {
		SortedSet<ItemStackHandlerSlot> matching = getSlotsContaining(variant.getItem());
		SortedSet<ItemStackHandlerSlot> empty = getSlotsContaining(Items.AIR);
		List<ItemStackHandlerSlot> insertableSlots = new ArrayList<>(matching.size() + empty.size());
		insertableSlots.addAll(matching);
		insertableSlots.addAll(empty);
		return insertableSlots.iterator();
	}

	private static SortedSet<ItemStackHandlerSlot> createSlotSet() {
		return new TreeSet<>(Comparator.comparingInt(ItemStackHandlerSlot::getIndex));
	}
}
