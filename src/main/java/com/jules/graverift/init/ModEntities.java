package com.jules.graverift.init;

import com.jules.graverift.GraveRiftMod;
import com.jules.graverift.entity.GraveRiftEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, GraveRiftMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GraveRiftEntity>> GRAVE_RIFT = ENTITY_TYPES.register("grave_rift",
            () -> EntityType.Builder.<GraveRiftEntity>of(GraveRiftEntity::new, MobCategory.MISC)
                    .sized(1.5F, 2.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("grave_rift"));
}
