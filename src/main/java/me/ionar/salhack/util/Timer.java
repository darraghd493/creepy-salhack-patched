package me.ionar.salhack.util;

public final class Timer {
    private long time;

    public Timer() {
        time = -1;
    }

    public boolean passed(double ms) {
        return System.currentTimeMillis() - this.time >= ms;
    }

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public void resetTimeSkipTo(long mS) {
        this.time = System.currentTimeMillis() + mS;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
