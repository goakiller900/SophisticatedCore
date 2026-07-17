package net.p3pp3rf1y.sophisticatedcore.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
//TODO after 1.22 remove support for legacy UUID deserialization via strings
public class SettingsTemplateStorage extends SavedData {
	private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
	private static final SavedDataType<SettingsTemplateStorage> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(SophisticatedCore.MOD_ID, "settings_templates"), SettingsTemplateStorage::new,
			RecordCodecBuilder.<SettingsTemplateStorage>create(builder -> builder.group(Codec
					.unboundedMap(UUID_CODEC, Codec.unboundedMap(CodecHelper.STRING_ENCODED_INT, CompoundTag.CODEC))
					.fieldOf("playerTemplates").forGetter(storage -> storage.playerTemplates),
					Codec.unboundedMap(UUID_CODEC, Codec.unboundedMap(ExtraCodecs.NON_EMPTY_STRING, CompoundTag.CODEC))
							.fieldOf("playerNamedTemplates").forGetter(storage -> storage.playerNamedTemplates))
					.apply(builder, SettingsTemplateStorage::new)), null);

	private Map<UUID, Map<Integer, CompoundTag>> playerTemplates = new HashMap<>();
	private Map<UUID, Map<String, CompoundTag>> playerNamedTemplates = new HashMap<>();
	private static final SettingsTemplateStorage clientStorageCopy = new SettingsTemplateStorage();

	private SettingsTemplateStorage() {
	}

	private SettingsTemplateStorage(Map<UUID, Map<Integer, CompoundTag>> playerTemplates,
			Map<UUID, Map<String, CompoundTag>> playerNamedTemplates) {
		this.playerTemplates = new HashMap<>();
		playerTemplates.forEach((playerId, templates) -> {
			Map<Integer, CompoundTag> copiedTemplates = new HashMap<>();
			templates.forEach((slot, data) -> {
				if (slot > 0) {
					copiedTemplates.put(slot, data);
				}
			});
			this.playerTemplates.put(playerId, copiedTemplates);
		});

		this.playerNamedTemplates = new HashMap<>();
		playerNamedTemplates.forEach((playerId, templates) -> {
			Map<String, CompoundTag> copiedTemplates = new TreeMap<>();
			templates.forEach((name, data) -> {
				if (!name.isEmpty()) {
					copiedTemplates.put(name, data);
				}
			});
			this.playerNamedTemplates.put(playerId, copiedTemplates);
		});
	}

	public void putPlayerTemplate(Player player, int slot, CompoundTag data) {
		if (slot <= 0) {
			return;
		}
		playerTemplates.computeIfAbsent(player.getUUID(), u -> new HashMap<>()).put(slot, data);
		setDirty();
	}

	public void putPlayerNamedTemplate(Player player, String name, CompoundTag data) {
		if (name.isEmpty()) {
			return;
		}
		playerNamedTemplates.computeIfAbsent(player.getUUID(), u -> new TreeMap<>()).put(name, data);
		setDirty();
	}

	public Map<Integer, CompoundTag> getPlayerTemplates(Player player) {
		return playerTemplates.getOrDefault(player.getUUID(), new HashMap<>());
	}

	public Map<String, CompoundTag> getPlayerNamedTemplates(Player player) {
		return playerNamedTemplates.getOrDefault(player.getUUID(), new TreeMap<>());
	}

	public static SettingsTemplateStorage get() {
		if (SophisticatedCore.isLogicalServerThread()) {
			MinecraftServer server = SophisticatedCore.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				// noinspection ConstantConditions - by this time overworld is loaded
				SavedDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(TYPE);
			}
		}
		return clientStorageCopy;
	}

	public void clearPlayerTemplates(Player player) {
		playerTemplates.remove(player.getUUID());
		playerNamedTemplates.remove(player.getUUID());
		setDirty();
	}
}
