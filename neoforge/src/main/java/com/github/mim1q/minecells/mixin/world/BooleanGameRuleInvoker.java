package com.github.mim1q.minecells.mixin.world;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanRule.class)
public interface BooleanGameRuleInvoker {
  @Invoker("create")
  static GameRules.Type<GameRules.BooleanRule> minecells$create(boolean defaultValue) {
    throw new AssertionError();
  }
}
