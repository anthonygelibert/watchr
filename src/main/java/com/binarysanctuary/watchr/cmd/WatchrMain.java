package com.binarysanctuary.watchr.cmd;

import com.binarysanctuary.watchr.Watchr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    /** Logger. */
    private static final Logger LOGGER = LogManager.getLogger(WatchrMain.class);

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
            LOGGER.fatal("Interrupting...."); // NON-NLS
            thread.interrupt();
            thread.join();
        }
    }
}
