package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.mixin.world.BooleanGameRuleInvoker;
import com.github.mim1q.minecells.mixin.world.GameRulesInvoker;
import net.minecraft.world.GameRules;

public class MineCellsGameRules {
  public static final GameRules.Key<GameRules.BooleanRule> MOBS_DROP_CELLS = GameRulesInvoker.minecells$register(
    "minecells.mobsDropCells",
    GameRules.Category.MOBS,
    BooleanGameRuleInvoker.minecells$create(false)
  );

  public static final GameRules.Key<GameRules.BooleanRule> SUFFOCATION_FIX = GameRulesInvoker.minecells$register(
    "minecells.suffocationFix",
    GameRules.Category.MISC,
    BooleanGameRuleInvoker.minecells$create(true)
  );

  public static void init() { }
}
