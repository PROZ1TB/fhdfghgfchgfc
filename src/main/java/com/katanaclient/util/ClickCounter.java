package com.katanaclient.util;

import java.util.ArrayDeque;

/** Counts clicks in a sliding one-second window. No Minecraft dependencies. */
public final class ClickCounter {
    private final ArrayDeque<Long> times = new ArrayDeque<>();
    private boolean wasDown = false;

    /** Call once per rendered frame with the current button state. */
    public void update(boolean isDown, long nowMs) {
        if (isDown && !wasDown) times.addLast(nowMs);
        wasDown = isDown;
        prune(nowMs);
    }

    public int cps(long nowMs) {
        prune(nowMs);
        return times.size();
    }

    private void prune(long nowMs) {
        while (!times.isEmpty() && nowMs - times.peekFirst() > 1000L) times.pollFirst();
    }

    public void reset() {
        times.clear();
        wasDown = false;
    }
}
