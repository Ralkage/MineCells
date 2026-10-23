package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.block.fluid.AncientSewageFluid;
import com.github.mim1q.minecells.block.fluid.SewageFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineCellsFluids {
  public static final FluidType SEWAGE_TYPE = new FluidType(
    FluidType.Properties.create().descriptionId("block.minecells.sewage")
  );
  public static final FluidType ANCIENT_SEWAGE_TYPE = new FluidType(
    FluidType.Properties.create().descriptionId("block.minecells.ancient_sewage")
  );

  public static FlowableFluid STILL_SEWAGE;
  public static FlowableFluid FLOWING_SEWAGE;

  public static FlowableFluid STILL_ANCIENT_SEWAGE;
  public static FlowableFluid FLOWING_ANCIENT_SEWAGE;

  public static void init() {
    STILL_SEWAGE = Registry.register(Registries.FLUID, MineCells.createId("sewage"), new SewageFluid.Still());
    FLOWING_SEWAGE = Registry.register(Registries.FLUID, MineCells.createId("flowing_sewage"), new SewageFluid.Flowing());
    STILL_ANCIENT_SEWAGE = Registry.register(Registries.FLUID, MineCells.createId("ancient_sewage"), new AncientSewageFluid.Still());
    FLOWING_ANCIENT_SEWAGE = Registry.register(Registries.FLUID, MineCells.createId("flowing_ancient_sewage"), new AncientSewageFluid.Flowing());
  }

  public static void registerFluidTypes() {
    Registry.register(NeoForgeRegistries.FLUID_TYPES, MineCells.createId("sewage"), SEWAGE_TYPE);
    Registry.register(NeoForgeRegistries.FLUID_TYPES, MineCells.createId("ancient_sewage"), ANCIENT_SEWAGE_TYPE);
  }
}
