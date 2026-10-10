package com.jules.graverift.event;

import com.jules.graverift.data.PlayerRiftSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // When a player dies and respawns, clear their active rift return entry so they are NOT teleported back
        if (event.isWasDeath()) {
            ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
            MinecraftServer server = newPlayer.getServer();
            if (server != null) {
                PlayerRiftSavedData savedData = PlayerRiftSavedData.get(server);
                savedData.removePlayerRiftEntry(newPlayer.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                PlayerRiftSavedData savedData = PlayerRiftSavedData.get(server);
                PlayerRiftSavedData.PlayerRiftEntry entry = savedData.getPlayerRiftEntry(player.getUUID());
                if (entry != null) {
                    long currentTime = player.level().getGameTime();
                    // If the rift expired while the player was offline
                    if (currentTime >= entry.expirationGameTime) {
                        ServerLevel targetLevel = server.getLevel(entry.returnDimension);
                        if (targetLevel == null) {
                            targetLevel = (ServerLevel) player.level();
                        }
                        // Teleport back to return position
                        player.teleportTo(targetLevel, entry.returnX, entry.returnY, entry.returnZ, entry.returnYRot, entry.returnXRot);

                        // Set food level to 0
                        player.getFoodData().setFoodLevel(0);
                        player.getFoodData().setSaturation(0.0F);

                        // Apply 10 minutes (12000 ticks) Weakness 1
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 12000, 0, false, true));

                        // Remove stored entry
                        savedData.removePlayerRiftEntry(player.getUUID());
                    }
                }
            }
        }
    }
}
