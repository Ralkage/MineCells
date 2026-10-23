package com.github.mim1q.minecells.mixin.client;

import com.github.mim1q.minecells.item.weapon.bow.CustomCrossbowItem;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
  // NeoForge 1.21.1 checks the item class here, instead of calling ItemStack.isOf.
  @Definition(id = "CrossbowItem", type = CrossbowItem.class)
  @Expression("? instanceof CrossbowItem")
  @ModifyExpressionValue(
    method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
    at = @At("MIXINEXTRAS:EXPRESSION")
  )
  private boolean minecells$includeCustomCrossbow(
    boolean original,
    AbstractClientPlayerEntity player,
    float tickDelta,
    float pitch,
    Hand hand,
    float swingProgress,
    ItemStack stack,
    float equipProgress,
    MatrixStack matrices,
    VertexConsumerProvider vertexConsumers,
    int light
  ) {
    return original || stack.getItem() instanceof CustomCrossbowItem;
  }
}
