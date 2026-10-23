package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.recipe.CellForgeRecipe;
import com.github.mim1q.minecells.recipe.CellForgeRecipeSerializer;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class MineCellsRecipeTypes {
  public static final RecipeType<CellForgeRecipe> CELL_FORGE_RECIPE_TYPE = new RecipeType<>() {
    @Override
    public String toString() {
      return "cell_forge_recipe";
    }
  };

  public static void registerType() {
    // The Fabric build used vanilla RecipeType.register, which stores this in the minecraft namespace.
    Registry.register(Registries.RECIPE_TYPE, Identifier.ofVanilla("cell_forge_recipe"), CELL_FORGE_RECIPE_TYPE);
  }

  public static void registerSerializer() {
    register("cell_forge_recipe", CellForgeRecipeSerializer.INSTANCE);
  }

  private static <T extends Recipe<?>> RecipeSerializer<T> register(String id, RecipeSerializer<T> serializer) {
    return Registry.register(Registries.RECIPE_SERIALIZER, MineCells.createId(id), serializer);
  }
}
