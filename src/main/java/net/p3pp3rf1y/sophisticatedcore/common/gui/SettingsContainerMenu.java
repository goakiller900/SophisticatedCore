package net.p3pp3rf1y.sophisticatedcore.common.gui;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.HashedStack;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.network.SyncAdditionalSlotInfoPayload;
import net.p3pp3rf1y.sophisticatedcore.network.SyncContainerClientDataPayload;
import net.p3pp3rf1y.sophisticatedcore.network.SyncEmptySlotIconsPayload;
import net.p3pp3rf1y.sophisticatedcore.network.SyncTemplateSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.settings.ISettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsContainerBase;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsTemplateStorage;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.util.DummySlot;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class SettingsContainerMenu<S extends IStorageWrapper> extends AbstractContainerMenu implements ISyncedContainer, IAdditionalSlotInfoMenu {
	private static final Map<String, ISettingsContainerFactory<?, ?>> SETTINGS_CONTAINER_FACTORIES = new HashMap<>();

	static {
		addFactory(MainSettingsCategory.NAME, MainSettingsContainer::new);
		addFactory(NoSortSettingsCategory.NAME, NoSortSettingsContainer::new);
		addFactory(MemorySettingsCategory.NAME, MemorySettingsContainer::new);
		addFactory(ItemDisplaySettingsCategory.NAME, ItemDisplaySettingsContainer::new);
	}

	protected final Player player;

	protected final S storageWrapper;

	private final List<Slot> storageInventorySlots = new ArrayList<>();
	public final NonNullList<ItemStack> lastGhostSlots = NonNullList.create();
	public final NonNullList<RemoteSlot> remoteGhostSlots = NonNullList.create();
	private final Map<String, SettingsContainerBase<?>> settingsContainers = new LinkedHashMap<>();
	private final TemplatePersistanceContainer templatePersistanceContainer;
	public final List<Slot> ghostSlots = new ArrayList<>();
	private boolean inventorySlotStackChanged = false;
	private final Set<Integer> inaccessibleSlots = new HashSet<>();
	private final Map<Integer, ItemStack> slotFilterItems = new HashMap<>();
	private final Map<Integer, Identifier> emptySlotIcons = new HashMap<>();

	protected SettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, S storageWrapper) {
		super(menuType, windowId);
		this.player = player;
		this.storageWrapper = storageWrapper;

		addStorageInventorySlots();
		addSettingsContainers();
		templatePersistanceContainer = new TemplatePersistanceContainer(this, player.level().registryAccess());

		// Workaround for EMI to show on settings screen - remove once API for arbitrary screens is available
		if (FabricLoader.getInstance().isModLoaded(CompatModIds.EMI)) {
			this.slots.add(DummySlot.INSTANCE);
		}
	}

	public int getNumberOfStorageInventorySlots() {
		return storageWrapper.getInventoryHandler().getSlotCount();
	}

	public S getStorageWrapper() {
		return storageWrapper;
	}

	private void addSettingsContainers() {
		SettingsHandler settingsHandler = storageWrapper.getSettingsHandler();
		settingsHandler.getSettingsCategories().forEach((name, category) -> settingsContainers.put(name, instantiateContainer(this, name, category)));
	}

	private void addStorageInventorySlots() {
		InventoryHandler inventoryHandler = storageWrapper.getInventoryHandler();

		int slotIndex = 0;

		while (slotIndex < inventoryHandler.getSlotCount()) {
			int finalSlotIndex = slotIndex;
			storageInventorySlots.add(addSlot(new ViewOnlyStorageInventorySlot(inventoryHandler, finalSlotIndex)));

			slotIndex++;
		}
	}

	public int getColumnsTaken() {
		return storageWrapper.getColumnsTaken();
	}

	@Override
	protected Slot addSlot(Slot slot) {
		slot.index = ghostSlots.size();
		ghostSlots.add(slot);
		lastGhostSlots.add(ItemStack.EMPTY);
		remoteGhostSlots.add(synchronizer != null ? synchronizer.createSlot() : RemoteSlot.PLACEHOLDER);
		return slot;
	}

	@Override
	public void broadcastChanges() {
		for (int slot = 0; slot < ghostSlots.size(); ++slot) {
			ItemStack itemstack = ghostSlots.get(slot).getItem();
			Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
			triggerSlotListeners(slot, itemstack, supplier);
			synchronizeSlotToRemote(slot, itemstack, supplier);
		}

		if (inventorySlotStackChanged) {
			inventorySlotStackChanged = false;
			sendAdditionalSlotInfo();
		}
	}

	@Override
	public void broadcastFullState() {
		for (int slotIndex = 0; slotIndex < ghostSlots.size(); ++slotIndex) {
			ItemStack itemstack = ghostSlots.get(slotIndex).getItem();
			triggerSlotListeners(slotIndex, itemstack, itemstack::copy);
		}

		sendAllDataToRemote();
	}

	@SuppressWarnings("java:S2177")
	private void triggerSlotListeners(int slotIndex, ItemStack slotStack, Supplier<ItemStack> slotStackCopy) {
		ItemStack itemstack = lastGhostSlots.get(slotIndex);
		if (!ItemStack.matches(itemstack, slotStack)) {
			ItemStack itemstack1 = slotStackCopy.get();
			lastGhostSlots.set(slotIndex, itemstack1);

			for (ContainerListener containerlistener : containerListeners) {
				containerlistener.slotChanged(this, slotIndex, itemstack1);
			}
		}

	}

	@SuppressWarnings("java:S2177")
	private void synchronizeSlotToRemote(int slotIndex, ItemStack slotStack, Supplier<ItemStack> slotStackCopy) {
		if (!suppressRemoteUpdates) {
			RemoteSlot remoteSlot = remoteGhostSlots.get(slotIndex);
			if (!remoteSlot.matches(slotStack)) {
				ItemStack stackCopy = slotStackCopy.get();
				remoteSlot.force(stackCopy);
				inventorySlotStackChanged = true;
				if (synchronizer != null) {
					synchronizer.sendSlotChange(this, slotIndex, stackCopy);
				}
			}
		}
	}

	public int getNumberOfSlots() {
		return storageWrapper.getInventoryHandler().getSlotCount();
	}

	@Override
	public void sendAllDataToRemote() {
		NonNullList<ItemStack> remoteStacks = NonNullList.create();
		for (int slotIndex = 0; slotIndex < ghostSlots.size(); slotIndex++) {
			ItemStack stack = ghostSlots.get(slotIndex).getItem().copy();
			remoteGhostSlots.get(slotIndex).force(stack);
			remoteStacks.add(stack);
		}

		if (synchronizer != null) {
			ItemStack carried = getCarried().copy();
			remoteCarried.force(carried);
			synchronizer.sendInitialData(this, remoteStacks, carried, new int[0]);
		}

		if (player instanceof ServerPlayer serverPlayer) {
			SettingsTemplateStorage settingsTemplateStorage = SettingsTemplateStorage.get();
			PacketDistributor.sendToPlayer(serverPlayer, new SyncTemplateSettingsPayload(settingsTemplateStorage.getPlayerTemplates(serverPlayer), settingsTemplateStorage.getPlayerNamedTemplates(serverPlayer)));
		}

		sendEmptySlotIcons();
		sendAdditionalSlotInfo();
	}

	public abstract void detectSettingsChangeAndReload();

	@Override
	public void setSynchronizer(ContainerSynchronizer synchronizer) {
		if (player instanceof ServerPlayer serverPlayer && storageWrapper.getInventoryHandler().getStackSizeMultiplier() > 1) {
			super.setSynchronizer(new HighStackCountSynchronizer(serverPlayer));
			return;
		}
		super.setSynchronizer(synchronizer);
	}

	@Override
	public Slot getSlot(int slotId) {
		return ghostSlots.get(slotId);
	}

	public void onMemorizedStackAdded(int slotNumber) {
		storageWrapper.getInventoryHandler().onSlotFilterChanged(slotNumber);
		sendAdditionalSlotInfo();
	}

	public void onMemorizedStackRemoved(int slotNumber) {
		if (getSlot(slotNumber).getItem().isEmpty()) {
			storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemChanged(slotNumber);
			storageWrapper.getInventoryHandler().onSlotFilterChanged(slotNumber);
			sendAdditionalSlotInfo();
		}
	}

	public void onMemorizedItemsChanged() {
		storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public List<Slot> getStorageInventorySlots() {
		return storageInventorySlots;
	}

	@Override
	public void handlePacket(CompoundTag data) {
		if (data.contains("categoryName")) {
			String categoryName = data.getStringOr("categoryName", "");
			if (settingsContainers.containsKey(categoryName)) {
				settingsContainers.get(categoryName).handlePacket(data);
			}
		} else if (data.get(TemplatePersistanceContainer.TEMPLATE_PERSISTANCE_TAG) instanceof CompoundTag) {
			templatePersistanceContainer.handlePacket(data.getCompoundOrEmpty(TemplatePersistanceContainer.TEMPLATE_PERSISTANCE_TAG));
		}
	}

	@Override
	public void clicked(int slotId, int dragType, ContainerInput clickTypeIn, Player player) {
		//noop
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	public void forEachSettingsContainer(BiConsumer<String, ? super SettingsContainerBase<?>> consumer) {
		settingsContainers.forEach(consumer);
	}

	public Player getPlayer() {
		return player;
	}

	public BlockPos getBlockPosition() {
		return BlockPos.ZERO;
	}


	public <T extends ISettingsCategory<?>> Optional<T> getSelectedTemplatesCategory(Class<T> categoryClass) {
		return templatePersistanceContainer.getSelectedTemplate().map(selectedTemplate -> selectedTemplate.getTypeCategory(categoryClass));
	}

	public TemplatePersistanceContainer getTemplatePersistanceContainer() {
		return templatePersistanceContainer;
	}

	public void refreshTemplateSlots() {
		templatePersistanceContainer.refreshTemplateSlots();
	}

	private class ViewOnlyStorageInventorySlot extends SlotItemHandler {
		public ViewOnlyStorageInventorySlot(IItemHandlerSimpleInserter inventoryHandler, int slotIndex) {
			super(inventoryHandler, slotIndex, 0, 0);
		}

		@Override
		public boolean mayPickup(Player playerIn) {
			return false;
		}

		@Nullable
		@Override
		public Identifier getNoItemIcon() {
			return inaccessibleSlots.contains(sophisticatedCore_getSlotIndex()) ? StorageContainerMenuBase.INACCESSIBLE_SLOT_BACKGROUND : emptySlotIcons.getOrDefault(sophisticatedCore_getSlotIndex(), null);
		}
	}

	public int getNumberOfRows() {
		return storageWrapper.getNumberOfSlotRows();
	}

	protected static <C extends ISettingsCategory<?>, T extends SettingsContainerBase<C>> void addFactory(String categoryName, ISettingsContainerFactory<C, T> factory) {
		SETTINGS_CONTAINER_FACTORIES.put(categoryName, factory);
	}

	public interface ISettingsContainerFactory<C extends ISettingsCategory<?>, T extends SettingsContainerBase<C>> {
		T create(SettingsContainerMenu<?> settingsContainer, String categoryName, C category);
	}

	private static <C extends ISettingsCategory<?>> SettingsContainerBase<C> instantiateContainer(SettingsContainerMenu<?> settingsContainer, String name, C category) {
		//noinspection unchecked
		return (SettingsContainerBase<C>) getSettingsContainerFactory(name).create(settingsContainer, name, category);
	}

	private static <C extends ISettingsCategory<?>, T extends SettingsContainerBase<C>> ISettingsContainerFactory<C, T> getSettingsContainerFactory(String name) {
		//noinspection unchecked
		return (ISettingsContainerFactory<C, T>) SETTINGS_CONTAINER_FACTORIES.get(name);
	}

	public void sendDataToServer(Supplier<CompoundTag> supplyData) {
		if (isServer()) {
			return;
		}
		CompoundTag data = supplyData.get();
		PacketDistributor.sendToServer(new SyncContainerClientDataPayload(data));
	}

	protected boolean isServer() {
		return !player.level().isClientSide();
	}

	public void sendAdditionalSlotInfo() {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		Set<Integer> inaccessibleSlots = new HashSet<>();
		InventoryHandler inventoryHandler = storageWrapper.getInventoryHandler();
		Map<Integer, Holder<Item>> slotFilterItems = new HashMap<>();
		for (int slot = 0; slot < inventoryHandler.getSlotCount(); slot++) {
			if (!inventoryHandler.isSlotAccessible(slot)) {
				inaccessibleSlots.add(slot);
			}

			if (inventoryHandler.getFilterItem(slot) != Items.AIR) {
				slotFilterItems.put(slot, inventoryHandler.getFilterItem(slot).builtInRegistryHolder());
			}
		}
		PacketDistributor.sendToPlayer(serverPlayer, new SyncAdditionalSlotInfoPayload(inaccessibleSlots, Map.of(), Set.of(), slotFilterItems));
	}

	@Override
	public void updateAdditionalSlotInfo(Set<Integer> inaccessibleSlots, Map<Integer, Integer> slotLimitOverrides, Set<Integer> infiniteSlots, Map<Integer, Holder<Item>> slotFilterItems) {
		this.inaccessibleSlots.clear();
		this.inaccessibleSlots.addAll(inaccessibleSlots);

		this.slotFilterItems.clear();
		slotFilterItems.forEach((slot, item) -> this.slotFilterItems.put(slot, new ItemStack(item)));
	}

	@Override
	public void updateEmptySlotIcons(Map<Identifier, Set<Integer>> emptySlotIcons) {
		this.emptySlotIcons.clear();
		emptySlotIcons.forEach((textureName, slots) -> slots.forEach(slot -> this.emptySlotIcons.put(slot, textureName)));
	}

	@Override
	public void setRemoteSlotUnsafe(int slot, HashedStack stack) {
		remoteGhostSlots.get(slot).receive(stack);
		inventorySlotStackChanged = true;
	}

	private void sendEmptySlotIcons() {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		Map<Identifier, Set<Integer>> noItemSlotTextures = new HashMap<>();
		for (int slot = 0; slot < storageWrapper.getInventoryHandler().getSlotCount(); slot++) {
			Pair<Identifier, Identifier> noItemIcon = storageWrapper.getInventoryHandler().getNoItemIcon(slot);
			if (noItemIcon != null) {
				noItemSlotTextures.computeIfAbsent(noItemIcon.getSecond(), rl -> new HashSet<>()).add(slot);
			}
		}
		PacketDistributor.sendToPlayer(serverPlayer, new SyncEmptySlotIconsPayload(noItemSlotTextures));
	}

	public ItemStack getSlotFilterItem(int slot) {
		return slotFilterItems.getOrDefault(slot, ItemStack.EMPTY);
	}
}
