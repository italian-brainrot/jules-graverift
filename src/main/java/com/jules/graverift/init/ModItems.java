package com.jules.graverift.init;

import com.jules.graverift.GraveRiftMod;
import com.jules.graverift.item.GraveRiftItem;
import com.jules.graverift.item.PiercedEnderpearlItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GraveRiftMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GraveRiftMod.MODID);

    public static final DeferredItem<Item> PIERCED_ENDERPEARL = ITEMS.register("pierced_enderpearl",
            () -> new PiercedEnderpearlItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> GRAVE_RIFT = ITEMS.register("grave_rift",
            () -> new GraveRiftItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("graverift_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jules_graverift"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> PIERCED_ENDERPEARL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PIERCED_ENDERPEARL.get());
                        output.accept(GRAVE_RIFT.get());
                    }).build());
}
