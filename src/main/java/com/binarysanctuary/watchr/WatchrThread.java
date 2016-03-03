package com.binarysanctuary.watchr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Thread watching for changes in specified directories and each directory created within watched directory tree.
 * Invokes provided callback whenever change is noticed.
 *
 * @author Michal Orman
 * @version 1.0.0
 */
public final class WatchrThread extends Thread {
    /** Logger. */
    private static final Logger LOGGER = LogManager.getLogger(WatchrThread.class);
    /** Callback. */
    private final OnChangeCallback    m_callback;
    /** Watched dirs. */
    private final Path[]              m_dirs;
    /** Watch service (JDK 7 object). */
    private final WatchService        m_watchService;
    /** Maps WatchKey's received during dir registration to dir which was registered. */
    private final Map<WatchKey, Path> m_dirsMapping;
    /** Boolean to control the thread. */
    private volatile boolean m_isRunning = true;

    /** Constructor with the callback and the dirs as {@link java.nio.file.Path}. */
    public WatchrThread(final OnChangeCallback callback, final Path... dirs) throws IOException {
        m_callback = callback;
        m_dirs = dirs.clone();
        m_dirsMapping = new HashMap<>(dirs.length);
        m_watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public void run() {
        try {
            register(m_dirs);

            while (m_isRunning) {
                final WatchKey key = m_watchService.take();

                final List<WatchEvent<?>> events = key.pollEvents();
                final Path dir = m_dirsMapping.get(key);

                m_callback.onChange(dir, events);

                for (final WatchEvent<?> event : events) {
                    if (Objects.equals(event.kind(), ENTRY_CREATE)) {
                        // Need to register to newly created directories
                        final Path name = (Path) event.context();
                        final Path child = dir.resolve(name);

                        if (Files.isDirectory(child)) {
                            registerTree(child);
                        }
                    }
                }

                if (!key.reset()) {
                    m_dirsMapping.remove(key);

                    if (m_dirsMapping.isEmpty()) {
                        break;
                    }
                }
            }
        }
        catch (final IOException e) {
            LOGGER.fatal("Watchr thread I/O exception", e);
        }
        catch (final InterruptedException ignore) {
            LOGGER.info("Watchr thread interrupted");
        }
        finally {
            try { m_watchService.close(); }
            catch (final IOException ignore) { /* IGNORE */ }
        }
    }

    public void interrupt() {
        m_isRunning = false;
    }

    @Override
    public String toString() {
        return String.format(
                "WatchrThread{m_callback=%s, m_dirs=%s, m_watchService=%s, m_dirsMapping=%s, m_isRunning=%s}",
                m_callback, Arrays.toString(m_dirs), m_watchService, m_dirsMapping, m_isRunning);
    }

    private void register(final Path... dirs) throws IOException {
        for (final Path dir : dirs) {
            registerTree(dir);
        }
    }

    private void registerTree(final Path child) throws IOException {
        Files.walkFileTree(child, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                    throws IOException {
                super.preVisitDirectory(dir, attrs);
                final WatchKey key = dir.register(m_watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                m_dirsMapping.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
