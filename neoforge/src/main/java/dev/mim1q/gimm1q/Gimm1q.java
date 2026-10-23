package dev.mim1q.gimm1q;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Namespace and diagnostics used by the original Gimm1q value calculators. */
public final class Gimm1q {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gimm1q/MineCells");
    public static boolean debugMessages = false;

    private Gimm1q() {}

    public static Identifier id(String path) {
        return Identifier.of("gimm1q", path);
    }
}
