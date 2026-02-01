package org.crowdwatch.rtm.infrastructure.utils;

import java.util.concurrent.Executor;

import io.vertx.mutiny.core.Context;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReactiveUtils {
    public static Executor createEventLoopExecutor(Context eventLoopContext) {
        if(!eventLoopContext.isEventLoopContext()) {
            throw new IllegalArgumentException("Context provided is not an event loop context");
        }
        return runnable -> eventLoopContext.runOnContext(() -> runnable.run());
    }
}
