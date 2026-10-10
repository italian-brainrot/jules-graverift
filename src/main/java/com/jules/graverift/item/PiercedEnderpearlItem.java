package com.jules.graverift.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class PiercedEnderpearlItem extends Item {
    public PiercedEnderpearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.jules_graverift.pierced_enderpearl.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
