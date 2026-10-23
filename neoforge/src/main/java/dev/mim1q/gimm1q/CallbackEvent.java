package dev.mim1q.gimm1q;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/** Small callback dispatcher replacing Fabric API's event helper. */
public final class CallbackEvent<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();
    private final T invoker;

    public CallbackEvent(Function<List<T>, T> factory) {
        invoker = factory.apply(listeners);
    }

    public void register(T listener) {
        listeners.add(listener);
    }

    public T invoker() {
        return invoker;
    }
}
