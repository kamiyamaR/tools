package stub.common.tool;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author kamiyama ryohei
 *
 */
public final class Stopwatch {
    private static final int DEFAULT_SIGNIFICANT_FIGIT = 4;
    private final Ticker ticker;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;

    /**
     *
     * @author agech
     *
     */
    public static class Ticker {

        /**
         *
         */
        protected Ticker() {
        }

        /**
         *
         * @return
         */
        public long read() {
            return System.nanoTime();
        }
    }

    /**
     *
     */
    public Stopwatch() {
        this.ticker = new Ticker();
    }

    /**
     *
     * @return
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     *
     * @return
     */
    public Stopwatch start() {
        checkState(!this.isRunning);
        this.isRunning = true;
        this.startTick = this.ticker.read();
        return this;
    }

    /**
     *
     * @param expression
     */
    public static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException("Illegal state:[" + expression + "]");
        }
    }

    /**
     *
     * @return
     */
    public Stopwatch stop() {
        long tick = this.ticker.read();
        checkState(this.isRunning);
        this.isRunning = false;
        this.elapsedNanos += tick - this.startTick;
        return this;
    }

    /**
     *
     * @return
     */
    public Stopwatch reset() {
        this.elapsedNanos = 0;
        this.isRunning = false;
        return this;
    }

    /**
     *
     * @return
     */
    private long elapsedNanos() {
        return this.isRunning ? this.ticker.read() - this.startTick + this.elapsedNanos : this.elapsedNanos;
    }

    /**
     *
     * @param desiredUnit
     * @return
     */
    public long elapsedTime(TimeUnit desiredUnit) {
        return desiredUnit.convert(this.elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     *
     * @return
     */
    public long elapsedMillis() {
        return elapsedTime(TimeUnit.MILLISECONDS);
    }

    /**
     *
     */
    @Override
    public String toString() {
        return toString(DEFAULT_SIGNIFICANT_FIGIT);
    }

    /**
     *
     * @param significantDigits
     * @return
     */
    public String toString(int significantDigits) {
        long nanos = this.elapsedNanos();
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / TimeUnit.NANOSECONDS.convert(1, unit);
        return String.format("%." + significantDigits + "g %s", value, abbreviate(unit));
    }

    /**
     *
     * @param nanos
     * @return
     */
    private static TimeUnit chooseUnit(long nanos) {
        if (TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.SECONDS;
        }

        if (TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.MILLISECONDS;
        }

        if (TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.MICROSECONDS;
        }

        return TimeUnit.NANOSECONDS;
    }

    /**
     *
     * @param unit
     * @return
     */
    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
        case NANOSECONDS:
            return "ns";
        case MICROSECONDS:
            return "\u03bcs";
        case MILLISECONDS:
            return "ms";
        case SECONDS:
            return "s";
        default:
            throw new AssertionError("Error unit:[" + unit + "]");
        }
    }
}
