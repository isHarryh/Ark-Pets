/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;


/** The abstract class that provides a thread-safe caching mechanism for value production.
 * @param <T> The type of the produced and cached value.
 */
public abstract class Cached<T> {
    protected T cachedValue;
    private boolean cacheEmpty;
    private long cacheNanoTime;
    private final Object lock;

    public Cached() {
        cachedValue = null;
        cacheEmpty = true;
        cacheNanoTime = 0L;
        lock = new Object();
    }

    /** Returns the cached value. If the cache has expired or is empty,
     * a new value is produced using {@link #produce}.
     * @return The cached or newly produced value.
     */
    public final T getValue() {
        long cacheAgeNanos = (long) (cacheAge() * 1_000_000_000L);
        if (cacheEmpty || System.nanoTime() > cacheAgeNanos + cacheNanoTime) {
            synchronized (lock) {
                long now = System.nanoTime();
                if (cacheEmpty || now > cacheAgeNanos + cacheAgeNanos) {
                    cachedValue = produce();
                    cacheEmpty = false;
                    cacheNanoTime = now;
                }
            }
        }
        return cachedValue;
    }

    /** Returns the cached value, regardless of whether it has expired or not.
     * @return The cached value (which may be null, indicating no value is cached).
     */
    public final T getCachedValue() {
        return cachedValue;
    }

    /** Resets the cache immediately.
     */
    public final void reset() {
        synchronized (lock) {
            cachedValue = null;
            cacheEmpty = true;
            cacheNanoTime = 0L;
        }
    }

    /** Produces a new value to be cached.
     * @return The newly produced value.
     */
    protected abstract T produce();

    /** Returns the cache age which will be applied to the {@link #getValue} request.
     * @return The cache age in second.
     */
    protected abstract double cacheAge();
}
