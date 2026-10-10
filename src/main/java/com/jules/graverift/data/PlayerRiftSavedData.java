package com.jules.graverift.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRiftSavedData extends SavedData {
    private static final String FILE_NAME = "jules_graverift_player_data";

    public static class PlayerRiftEntry {
        public UUID riftUUID;
        public ResourceKey<Level> returnDimension;
        public double returnX, returnY, returnZ;
        public float returnYRot, returnXRot;
        public long expirationGameTime;

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            if (riftUUID != null) {
                tag.putUUID("RiftUUID", riftUUID);
            }
            if (returnDimension != null) {
                tag.putString("ReturnDimension", returnDimension.location().toString());
            }
            tag.putDouble("ReturnX", returnX);
            tag.putDouble("ReturnY", returnY);
            tag.putDouble("ReturnZ", returnZ);
            tag.putFloat("ReturnYRot", returnYRot);
            tag.putFloat("ReturnXRot", returnXRot);
            tag.putLong("ExpirationGameTime", expirationGameTime);
            return tag;
        }

        public static PlayerRiftEntry load(CompoundTag tag) {
            PlayerRiftEntry entry = new PlayerRiftEntry();
            if (tag.hasUUID("RiftUUID")) {
                entry.riftUUID = tag.getUUID("RiftUUID");
            }
            if (tag.contains("ReturnDimension")) {
                entry.returnDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("ReturnDimension")));
            }
            entry.returnX = tag.getDouble("ReturnX");
            entry.returnY = tag.getDouble("ReturnY");
            entry.returnZ = tag.getDouble("ReturnZ");
            entry.returnYRot = tag.getFloat("ReturnYRot");
            entry.returnXRot = tag.getFloat("ReturnXRot");
            entry.expirationGameTime = tag.getLong("ExpirationGameTime");
            return entry;
        }
    }

    private final Map<UUID, PlayerRiftEntry> activeEntries = new HashMap<>();

    public PlayerRiftSavedData() {}

    public static PlayerRiftSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return new PlayerRiftSavedData();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(PlayerRiftSavedData::new, PlayerRiftSavedData::load, null),
                FILE_NAME
        );
    }

    public static PlayerRiftSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerRiftSavedData data = new PlayerRiftSavedData();
        if (tag.contains("PlayerEntries", Tag.TAG_LIST)) {
            ListTag list = tag.getList("PlayerEntries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                UUID playerUUID = entryTag.getUUID("PlayerUUID");
                PlayerRiftEntry entry = PlayerRiftEntry.load(entryTag);
                data.activeEntries.put(playerUUID, entry);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerRiftEntry> entry : activeEntries.entrySet()) {
            CompoundTag entryTag = entry.getValue().save();
            entryTag.putUUID("PlayerUUID", entry.getKey());
            list.add(entryTag);
        }
        tag.put("PlayerEntries", list);
        return tag;
    }

    public void setPlayerRiftEntry(UUID playerUUID, PlayerRiftEntry entry) {
        activeEntries.put(playerUUID, entry);
        setDirty();
    }

    public PlayerRiftEntry getPlayerRiftEntry(UUID playerUUID) {
        return activeEntries.get(playerUUID);
    }

    public void removePlayerRiftEntry(UUID playerUUID) {
        if (activeEntries.remove(playerUUID) != null) {
            setDirty();
        }
    }
}
