package com.jules.graverift.init;

import com.jules.graverift.GraveRiftMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, GraveRiftMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_RIFT_OPEN = SOUNDS.register("grave_rift_open",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GraveRiftMod.MODID, "grave_rift_open")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_RIFT_HUM = SOUNDS.register("grave_rift_hum",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GraveRiftMod.MODID, "grave_rift_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_RIFT_CLOSE = SOUNDS.register("grave_rift_close",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GraveRiftMod.MODID, "grave_rift_close")));
}
