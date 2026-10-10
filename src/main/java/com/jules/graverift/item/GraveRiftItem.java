package com.jules.graverift.item;

import com.jules.graverift.entity.GraveRiftEntity;
import com.jules.graverift.init.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GraveRiftItem extends Item {
    public GraveRiftItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        Level level = player.level();
        if (!level.isClientSide()) {
            // Apply 10 HP damage to target entity
            interactionTarget.hurt(level.damageSources().magic(), 10.0F);

            // Spawn GraveRiftEntity at target location
            GraveRiftEntity rift = ModEntities.GRAVE_RIFT.get().create(level);
            if (rift != null) {
                rift.moveTo(interactionTarget.getX(), interactionTarget.getY(), interactionTarget.getZ(), interactionTarget.getYRot(), interactionTarget.getXRot());
                rift.setOpenerUUID(player.getUUID());
                level.addFreshEntity(rift);
            }

            // Apply Hunger effect level 15 (amplifier 14) for 1 minute (1200 ticks)
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 30, false, true));

            // Consume item unless creative mode
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.jules_graverift.grave_rift.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
