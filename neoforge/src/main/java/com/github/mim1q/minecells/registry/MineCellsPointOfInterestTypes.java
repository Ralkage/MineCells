package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Set;

public class MineCellsPointOfInterestTypes {
  public static final PointOfInterestType KINGDOM_PORTAL = Registry.register(
    Registries.POINT_OF_INTEREST_TYPE,
    MineCells.createId("kingdom_portal"),
    new PointOfInterestType(Set.copyOf(MineCellsBlocks.KINGDOM_PORTAL_CORE.getStateManager().getStates()), 1, 1)
  );

  public static void init() { }
}
