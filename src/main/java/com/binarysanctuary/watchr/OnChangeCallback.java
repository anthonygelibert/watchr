package com.binarysanctuary.watchr;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

/**
 * Callback invoked whenever change in certain directory is noticed.
 *
 * @author Michal Orman
 * @version 1.0.0
 */
@FunctionalInterface
public interface OnChangeCallback {

    /**
     * Method called when there is an event in the tracked folder.
     *
     * @param dir    folder to track
     * @param events kinds of events to track
     */
    void onChange(Path dir, List<WatchEvent<?>> events);

}
