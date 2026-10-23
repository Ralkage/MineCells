package com.github.mim1q.minecells.data.spawner_runes;

import com.github.mim1q.minecells.MineCells;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads spawner rune datapack definitions on the resource preparation thread. */
public class SpawnerRunesReloadListener extends SinglePreparationResourceReloader<Map<Identifier, SpawnerRuneData>> {
  private final Map<Identifier, SpawnerRuneData> data = new HashMap<>();

  @Override
  protected Map<Identifier, SpawnerRuneData> prepare(ResourceManager manager, Profiler profiler) {
    MineCells.LOGGER.info("Reloading Spawner Rune data...");
    var loaded = new HashMap<Identifier, SpawnerRuneData>();
    manager.findResources("spawner_runes", id -> id.getPath().endsWith(".json"))
      .forEach((id, resource) -> {
        var dataId = Identifier.of(
          id.getNamespace(),
          id.getPath().substring("spawner_runes/".length(), id.getPath().length() - ".json".length())
        );
        try (var reader = new InputStreamReader(resource.getInputStream())) {
          SpawnerRuneData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
            .resultOrPartial(error -> MineCells.LOGGER.error("Failed to parse spawner rune data {}: {}", dataId, error))
            .ifPresent(result -> loaded.put(dataId, result));
        } catch (IOException e) {
          MineCells.LOGGER.error("Failed to read resource: {}", id, e);
        }
      });
    return loaded;
  }

  @Override
  protected void apply(Map<Identifier, SpawnerRuneData> loaded, ResourceManager manager, Profiler profiler) {
    data.clear();
    data.putAll(loaded);
  }

  public SpawnerRuneData get(Identifier id) {
    return data.get(id);
  }

  public SpawnerRuneData get(String id) {
    return get(Identifier.tryParse(id));
  }

  public List<Identifier> getIds() {
    return List.copyOf(data.keySet());
  }
}
