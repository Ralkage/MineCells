// Adapted from Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.client.item.handheld;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HandheldItemModelRegistryImpl implements HandheldItemModelRegistry {
    public static final HandheldItemModelRegistry INSTANCE = new HandheldItemModelRegistryImpl();
    public static final Map<Item, Pair<Identifier, Identifier>> MODELS = new ConcurrentHashMap<>();

    private HandheldItemModelRegistryImpl() {}

    @Override
    public void register(Item item, Identifier modelId, Identifier handheldId) {
        MODELS.put(item, new Pair<>(modelId.withPrefixedPath("item/"), handheldId.withPrefixedPath("item/")));
    }
}
