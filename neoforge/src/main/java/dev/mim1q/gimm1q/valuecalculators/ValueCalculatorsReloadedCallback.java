// Adapted from Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.valuecalculators;

import dev.mim1q.gimm1q.CallbackEvent;
import net.minecraft.util.Identifier;
import java.util.Set;

@FunctionalInterface
public interface ValueCalculatorsReloadedCallback {
    CallbackEvent<ValueCalculatorsReloadedCallback> EVENT = new CallbackEvent<>(listeners -> ids -> {
        for (var listener : listeners) listener.onValueCalculatorsReloaded(ids);
    });

    void onValueCalculatorsReloaded(Set<Identifier> ids);
}
