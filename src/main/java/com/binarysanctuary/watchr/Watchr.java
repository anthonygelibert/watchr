package com.binarysanctuary.watchr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Arrays;

/**
 * Watch for changes in specified set of directories. It is a high level abstraction
 * over JDK 7 {@link WatchService}.
 * <p>
 * The <tt>Watchr</tt> setups thread that is responsible for watching for changes in
 * specified directories and invokes callback whenever change is noticed. <tt>Watchr</tt>
 * watches for CREATE, MODIFY or DELETE events in all provided directories and subdirectories.
 * Each directory created inside watched directory is also registered for watch.
 *
 * @author Michal Orman
 * @version 1.0.0
 */
public final class Watchr {
    /** Forbidden. */
    private Watchr() {}

    /**
     * Call <i>callback</i> on each events in the <i>dirs</i>.
     *
     * @see OnChangeCallback
     */
    public static Thread watch(final OnChangeCallback callback, final String... dirs) throws IOException {
        return watch(callback, Paths.get(dirs[0], Arrays.copyOfRange(dirs, 1, dirs.length)));
    }

    /**
     * Call <i>callback</i> on each events in the <i>dirs</i>.
     *
     * @see OnChangeCallback
     */
    public static Thread watch(final OnChangeCallback callback, final Path... dirs) throws IOException {
        final Thread thread = new Thread(new WatchrThread(callback, dirs));
        thread.setDaemon(true);
        thread.setName("Watchr Monitoring Thread"); /* NON-NLS */
        thread.start();
        return thread;
    }
}
