package com.jules.graverift;

import com.jules.graverift.init.ModEntities;
import com.jules.graverift.init.ModItems;
import com.jules.graverift.init.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(GraveRiftMod.MODID)
public class GraveRiftMod {
    public static final String MODID = "jules_graverift";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GraveRiftMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Jules Grave Rift Mod");

        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
    }
}
