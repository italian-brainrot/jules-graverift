package com.jules.graverift.entity;

import com.jules.graverift.data.PlayerRiftSavedData;
import com.jules.graverift.init.ModSounds;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GraveRiftEntity extends Entity {
    private static final int MAX_LIFETIME = 1200; // 1 minute (1200 ticks)
    private static final int NAUSEA_TICK = 1000;  // 10 seconds remaining (1000 ticks)

    private UUID openerUUID;
    private final Set<UUID> enteredPlayerUUIDs = new HashSet<>();
    private int lifetime = 0;

    public GraveRiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public void setOpenerUUID(UUID openerUUID) {
        this.openerUUID = openerUUID;
    }

    public UUID getOpenerUUID() {
        return openerUUID;
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;

        if (!level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level();

            // Sound at opening
            if (lifetime == 1) {
                serverLevel.playSound(null, getX(), getY(), getZ(), ModSounds.GRAVE_RIFT_OPEN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            }

            // Hum sound periodically
            if (lifetime % 40 == 0) {
                serverLevel.playSound(null, getX(), getY(), getZ(), ModSounds.GRAVE_RIFT_HUM.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
            }

            // Detect player collisions
            AABB boundingBox = this.getBoundingBox().inflate(0.5);
            for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, boundingBox)) {
                if (!enteredPlayerUUIDs.contains(player.getUUID())) {
                    handlePlayerEntry(player);
                }
            }

            // 10 seconds before expiration (at tick 1000)
            if (lifetime == NAUSEA_TICK) {
                applyNauseaToEnteredPlayers(serverLevel);
            }

            // At expiration (1200 ticks)
            if (lifetime >= MAX_LIFETIME) {
                expireRift(serverLevel);
            }
        } else {
            // Client-side visual particle effects
            spawnClientParticles();
        }
    }

    private void handlePlayerEntry(ServerPlayer player) {
        Optional<GlobalPos> deathLocOpt = player.getLastDeathLocation();
        if (deathLocOpt.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.jules_graverift.no_death_location"));
            return;
        }

        GlobalPos deathLoc = deathLocOpt.get();
        ServerLevel destLevel = player.getServer().getLevel(deathLoc.dimension());

        if (destLevel == null) {
            destLevel = (ServerLevel) player.level();
        }

        // Save return data for offline/relog safety
        PlayerRiftSavedData savedData = PlayerRiftSavedData.get(player.getServer());
        PlayerRiftSavedData.PlayerRiftEntry entry = new PlayerRiftSavedData.PlayerRiftEntry();
        entry.riftUUID = this.getUUID();
        entry.returnDimension = player.level().dimension();
        entry.returnX = player.getX();
        entry.returnY = player.getY();
        entry.returnZ = player.getZ();
        entry.returnYRot = player.getYRot();
        entry.returnXRot = player.getXRot();
        entry.expirationGameTime = player.level().getGameTime() + (MAX_LIFETIME - lifetime);

        savedData.setPlayerRiftEntry(player.getUUID(), entry);
        enteredPlayerUUIDs.add(player.getUUID());

        // Teleport player to last death location
        player.teleportTo(destLevel, deathLoc.pos().getX() + 0.5, deathLoc.pos().getY() + 0.5, deathLoc.pos().getZ() + 0.5, player.getYRot(), player.getXRot());

        // Remove all player's XP
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.experienceProgress = 0.0F;

        // Apply initial status effects:
        // Fire Resistance I (10s = 200 ticks)
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true));
        // Water Breathing I (10s = 200 ticks)
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, true));
        // Invisibility level 14 (amplifier 13) (10s = 200 ticks)
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 13, false, true));
        // Resistance level 11 (amplifier 10) (5s = 100 ticks)
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 10, false, true));
    }

    private void applyNauseaToEnteredPlayers(ServerLevel serverLevel) {
        for (UUID uuid : enteredPlayerUUIDs) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && player.isAlive()) {
                // Cover the remaining lifetime plus a one-second buffer so the warning is still
                // ticking when the rift collapses (expireRift clears it on arrival).
                int remaining = MAX_LIFETIME - lifetime;
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, remaining + 20, 0, false, true));
            }
        }
    }

    private void expireRift(ServerLevel serverLevel) {
        serverLevel.playSound(null, getX(), getY(), getZ(), ModSounds.GRAVE_RIFT_CLOSE.get(), SoundSource.AMBIENT, 1.0F, 1.0F);

        PlayerRiftSavedData savedData = PlayerRiftSavedData.get(serverLevel.getServer());
        for (UUID uuid : enteredPlayerUUIDs) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            PlayerRiftSavedData.PlayerRiftEntry returnEntry = savedData.getPlayerRiftEntry(uuid);

            if (player != null && player.isAlive() && returnEntry != null) {
                ServerLevel targetLevel = serverLevel.getServer().getLevel(returnEntry.returnDimension);
                if (targetLevel == null) targetLevel = serverLevel;

                // Teleport back
                player.teleportTo(targetLevel, returnEntry.returnX, returnEntry.returnY, returnEntry.returnZ, returnEntry.returnYRot, returnEntry.returnXRot);

                // End the Nausea warning exactly when the player is pulled back.
                player.removeEffect(MobEffects.CONFUSION);

                // Set food level to 0
                player.getFoodData().setFoodLevel(0);
                player.getFoodData().setSaturation(0.0F);

                // Apply 10 minutes (12000 ticks) Weakness 1 (amplifier 0)
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 12000, 0, false, true));

                // Clear stored rift data
                savedData.removePlayerRiftEntry(uuid);
            }
        }

        this.discard();
    }

    private void spawnClientParticles() {
        for (int i = 0; i < 3; i++) {
            double rx = (random.nextDouble() - 0.5) * 1.5;
            double ry = random.nextDouble() * 2.5;
            double rz = (random.nextDouble() - 0.5) * 1.5;
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + rx, getY() + ry, getZ() + rz, 0, 0.02, 0);
            level().addParticle(ParticleTypes.PORTAL, getX() + rx, getY() + ry, getZ() + rz, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2);
            level().addParticle(ParticleTypes.REVERSE_PORTAL, getX() + rx, getY() + ry, getZ() + rz, 0, 0.05, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.lifetime = compound.getInt("Lifetime");
        if (compound.hasUUID("OpenerUUID")) {
            this.openerUUID = compound.getUUID("OpenerUUID");
        }
        enteredPlayerUUIDs.clear();
        if (compound.contains("EnteredPlayers", Tag.TAG_LIST)) {
            ListTag list = compound.getList("EnteredPlayers", Tag.TAG_INT_ARRAY);
            for (Tag tag : list) {
                enteredPlayerUUIDs.add(NbtUtils.loadUUID(tag));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Lifetime", lifetime);
        if (openerUUID != null) {
            compound.putUUID("OpenerUUID", openerUUID);
        }
        ListTag list = new ListTag();
        for (UUID uuid : enteredPlayerUUIDs) {
            list.add(NbtUtils.createUUID(uuid));
        }
        compound.put("EnteredPlayers", list);
    }
}
