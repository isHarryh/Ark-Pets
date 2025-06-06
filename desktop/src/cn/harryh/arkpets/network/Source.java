/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network;

import cn.harryh.arkpets.utils.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Source {
    public final String tag;
    public final String preUrl;
    public int delay = -1;
    public long lastErrorTime = -1;

    public Source(String tag, String preUrl) {
        this.tag = tag;
        this.preUrl = preUrl;
    }

    public void receiveError() {
        lastErrorTime = System.currentTimeMillis();
        Logger.debug("Network", "Marked source \"" + tag + "\" as historical unavailable with timestamp " + lastErrorTime);
    }

    public void testDelay() {
        testDelay(443, 1500);
    }

    public void testDelay(int port, int timeoutMillis) {
        delay = NetworkUtils.testDelay(preUrl, port, timeoutMillis);
        Logger.debug("Network", "Real delay for \"" + tag + "\" is " + delay + "ms");
    }

    @Override
    public String toString() {
            return getClass().getSimpleName() + " \"" + tag + "\" (" + delay + "ms)";
        }


    public static class GitHubSource extends Source {
        public static final ArrayList<GitHubSource> ghSources;
        static {
            ghSources = new ArrayList<>();
            ghSources.add(new GitHubSource("GitHub",
                    "https://raw.githubusercontent.com/",
                    "https://github.com/"));
            ghSources.add(new GitHubSource("GHProxy",
                    "https://ghproxy.harryh.cn/https://raw.githubusercontent.com/",
                    "https://ghproxy.harryh.cn/https://github.com/"));
        }

        public final String rawPreUrl;
        public final String archivePreUrl;

        public GitHubSource(String tag, String rawPreUrl, String archivePreUrl) {
            super(tag, rawPreUrl);
            this.rawPreUrl = rawPreUrl;
            this.archivePreUrl = archivePreUrl;
        }

        public static GitHubSource getMostAvailable() {
            Logger.info("Network", "Testing real delay");
            ghSources.forEach(Source::testDelay);
            List<GitHubSource> sorted = ghSources.stream()
                    .sorted(new DelayComparator())
                    .sorted(new AvailabilityComparator())
                    .toList();
            GitHubSource selectedSource = sorted.get(0);
            Logger.info("Network", "Selected the most available " + selectedSource);
            return selectedSource;
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
            if (o1.lastErrorTime != o2.lastErrorTime)
                return (o1.lastErrorTime > o2.lastErrorTime) ? 1 : -1;
            return 0;
        }
    }
}
