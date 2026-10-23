// Adapted from Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.client.tooltip;

import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

@ApiStatus.Internal
public class TooltipResolverRegistryImpl implements TooltipResolverRegistry {
    private static final HashMap<Item, TooltipResolver> resolvers = new HashMap<>();

    static TooltipResolverRegistry INSTANCE = new TooltipResolverRegistryImpl();

    @Override
    public void register(TooltipResolver resolver, Item... items) {
        for (Item item : items) {
            resolvers.put(item, resolver);
        }
    }

    @Override
    public TooltipResolver getResolver(Item item) {
        return resolvers.get(item);
    }
}
