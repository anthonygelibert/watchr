package com.binarysanctuary.watchr.cmd;

import com.binarysanctuary.watchr.Watchr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * Watch the given folders.
 *
 * @author Michal Orman
 * @version 1.0.0
 */
public final class WatchrMain {

    /** Watch the given folders. */
    public static void main(final String... args) throws IOException, InterruptedException {
        if (args.length > 0) {
            final Thread thread = Watchr.watch((dir, events) -> {
                for (final WatchEvent<?> event : events) {
                    final Path file = dir.resolve((Path) event.context());
                    LOGGER.info("Notified: {} on file: {}", event.kind(), file); // NON-NLS
                }
            }, args);

            System.in.read();
            System.out.println("Interrupting....");
            thread.interrupt();
            thread.join();
        }
    }
}
