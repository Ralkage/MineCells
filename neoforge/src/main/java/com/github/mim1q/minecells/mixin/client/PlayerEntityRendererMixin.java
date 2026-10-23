package com.github.mim1q.minecells.mixin.client;

import com.github.mim1q.minecells.item.weapon.bow.CustomCrossbowItem;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
  @Definition(id = "CrossbowItem", type = CrossbowItem.class)
  @Expression("? instanceof CrossbowItem")
  @ModifyExpressionValue(method = "getArmPose", at = @At("MIXINEXTRAS:EXPRESSION"))
  private static boolean minecells$includeCustomCrossbowArmPose(boolean original, AbstractClientPlayerEntity player, Hand hand) {
    return original || player.getStackInHand(hand).getItem() instanceof CustomCrossbowItem;
  }
}
