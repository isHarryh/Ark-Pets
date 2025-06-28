/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import cn.harryh.arkpets.utils.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.*;


public class SourceStrategy {
    protected static final HashMap<String, SourceStrategy> STRATEGY_POOL = new HashMap<>();

    protected Source primarySource;
    protected final ArrayList<Source> backupSources;

    public SourceStrategy() {
        primarySource = null;
        backupSources = new ArrayList<>();
    }

    public SourceStrategy setPrimarySource(Source source) {
        primarySource = Objects.requireNonNull(source);
        return this;
    }

    public SourceStrategy setPrimarySource(String name, String url) {
        return setPrimarySource(new Source(name, url));
    }

    public SourceStrategy clearPrimarySource() {
        primarySource = null;
        return this;
    }

    public SourceStrategy addBackupSource(Source source) {
        backupSources.add(Objects.requireNonNull(source));
        return this;
    }

    public SourceStrategy addBackupSource(String name, String url) {
        return addBackupSource(new Source(name, url));
    }
    
    public SourceStrategy clearBackupSource() {
        backupSources.clear();
        return this;
    }

    public Source getBestSource() {
        if (primarySource != null)
            return primarySource;
        if (backupSources.isEmpty())
            throw new IllegalStateException("No source found in this strategy");
        if (backupSources.size() == 1)
            return backupSources.get(0);
        Logger.info("Network", "Testing real delay");
        backupSources.forEach(Source::testDelay);
        List<Source> sorted = backupSources.stream()
                .sorted(new DelayComparator())
                .sorted(new AvailabilityComparator())
                .toList();
        Source selectedSource = sorted.get(0);
        Logger.info("Network", "Selected the most available " + selectedSource);
        return selectedSource;
    }

    public static SourceStrategy registerStrategy(String name) {
        if (STRATEGY_POOL.containsKey(name))
            throw new IllegalStateException("Duplicated strategy name: " + name);
        STRATEGY_POOL.put(name, new SourceStrategy());
        return STRATEGY_POOL.get(name);
    }

    public static SourceStrategy getStrategy(String name) {
        if (!STRATEGY_POOL.containsKey(name))
            throw new IllegalStateException("No such strategy name: " + name);
        return STRATEGY_POOL.get(name);
    }


    public static class Source {
        protected final String tag;
        protected final String url;
        protected int delay = -1;
        protected long lastErrorNanoTime = -1;

        public Source(String tag, String url) {
            this.tag = tag;
            this.url = url;
        }

        public URL toURL() {
            try {
                return new URL(url);
            } catch (Exception e) {
                throw new RuntimeException("Invalid URL format: " + url, e);
            }
        }

        public void receiveError() {
            lastErrorNanoTime = System.nanoTime();
            Logger.debug("Network", "Marked source \"" + tag + "\" as an unavailable source");
        }

        public void testDelay() {
            testDelay(443, 1500);
        }

        public void testDelay(int port, int timeoutMillis) {
            Socket socket = new Socket();
            int delayMillis = -1;
            try {
                SocketAddress address = new InetSocketAddress(new URL(url).getHost(), port);
                long start = System.nanoTime();
                socket.connect(address, timeoutMillis);
                long stop = System.nanoTime();
                delayMillis = (int) ((stop - start) / 1_000_000L);
            } catch (IOException ignored) {
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
            delay = delayMillis;
            Logger.debug("Network", "Real delay for \"" + tag + "\" is " + delay + "ms");
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " \"" + tag + "\" (" + delay + "ms)";
        }
    }


    public static class DelayComparator implements Comparator<Source> {
        @Override
        public int compare(Source o1, Source o2) {
            if (o1.delay == o2.delay)
                return 0;
            if (o1.delay < 0 && o2.delay >= 0)
                return 1;
            if (o1.delay >= 0 && o2.delay < 0)
                return -1;
            return (o1.delay > o2.delay) ? 1 : -1;
        }
    }


    public static class AvailabilityComparator implements Comparator<Source> {
        @Override
        public int compare(Source o1, Source o2) {
            if (o1.lastErrorNanoTime != o2.lastErrorNanoTime)
                return (o1.lastErrorNanoTime > o2.lastErrorNanoTime) ? 1 : -1;
            return 0;
        }
    }
}
