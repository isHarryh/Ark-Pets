/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.util.Objects;
import java.util.function.Supplier;


/** The class that provides a thread-safe caching mechanism for value production.
 * @param <T> The type of the produced and cached value.
 */
public final class Cached<T> {
    private T cachedValue;
    private long cacheNanoTime;
    private double fixedCacheAge;

    private boolean valueChangedFlag;
    private boolean valueEmptyFlag;

    private Supplier<T> valueProducer;
    private Supplier<Double> cacheAgeProducer;

    private final Object lock;

    public Cached() {
        cachedValue = null;
        cacheNanoTime = 0L;
        fixedCacheAge = 0.0;
        valueChangedFlag = false;
        valueEmptyFlag = true;
        valueProducer = null;
        cacheAgeProducer = null;
        lock = new Object();
    }

    /** Gets the current value.
     * <p>
     * The current value will be recomputed if the cache is expired and the value producer is set.
     * <p>
     * The current value can be {@code null} if the cache is empty but no producer is set
     * or the cached value is literally {@code null}.
     * @return The current value.
     */
    public T getValue() {
        synchronized (lock) {
            if (valueProducer != null && isExpired()) {
                setValue(valueProducer.get());
            }
            return cachedValue;
        }
    }

    /** Gets the currently cached value without triggering recomputation.
     * <p>
     * The cached value can be {@code null} if the cache is empty or the cached value is literally {@code null}.
     * @return The cached value.
     */
    public T getCachedValue() {
        synchronized (lock) {
            return cachedValue;
        }
    }

    /** Removes the cached value immediately, making the cache empty.
     */
    public void removeCachedValue() {
        synchronized (lock) {
            valueChangedFlag = !valueEmptyFlag;
            valueEmptyFlag = true;
            cachedValue = null;
            cacheNanoTime = 0L;
        }
    }

    /** Sets a new value directly and marks the cache as up-to-date.
     * @param newValue The new value to cache.
     */
    public void setValue(T newValue) {
        synchronized (lock) {
            valueChangedFlag |= !Objects.equals(newValue, cachedValue);
            valueEmptyFlag = false;
            cachedValue = newValue;
            cacheNanoTime = System.nanoTime();
        }
    }

    /** Sets a producer for generating values when recomputation is needed.
     * @param producer The value producer.
     */
    public void setValueProducer(Supplier<T> producer) {
        synchronized (lock) {
            valueProducer = Objects.requireNonNull(producer);
        }
    }

    /** Sets a fixed cache age.
     * <p>
     * Note that this may take no effect if a cache age producer is set.
     * @param seconds The cache age in seconds. A non-positive cache age means do not cache.
     */
    public void setCacheAge(double seconds) {
        synchronized (lock) {
            fixedCacheAge = seconds;
        }
    }

    /** Sets a producer for dynamic cache age determination.
     * @param producer The cache age producer.
     */
    public void setCacheAgeProducer(Supplier<Double> producer) {
        synchronized (lock) {
            cacheAgeProducer = Objects.requireNonNull(producer);
        }
    }

    /** Checks if the value has changed since the last {@link #getValue()} operation.
     * @return True if changed, false otherwise.
     */
    public boolean isChanged() {
        synchronized (lock) {
            return valueChangedFlag;
        }
    }

    /** Checks if the value is expired.
     * <p>
     * Note that an empty (unset) cache is also considered expired.
     * @return True if expired, false otherwise.
     */
    public boolean isExpired() {
        synchronized (lock) {
            if (valueEmptyFlag) {
                return true;
            }
            double cacheAge = cacheAgeProducer == null ? fixedCacheAge : cacheAgeProducer.get();
            return System.nanoTime() > cacheNanoTime + cacheAge * 1_000_000_000L;
        }
    }
}
