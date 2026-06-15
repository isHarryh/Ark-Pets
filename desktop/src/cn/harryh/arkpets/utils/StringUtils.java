/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.time.Duration.between;


public class StringUtils {
    private static final Map<Long, String> sizeMap = Map.of(
            1L, "B",
            1L << 10, "KB",
            1L << 20, "MB",
            1L << 30, "GB",
            1L << 40, "TB"
    );
    private static final DecimalFormat sizeFormat = new DecimalFormat("0.0");
    private static final DateTimeFormatter simpleDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /** Gets a formatted size string, e.g."{@code 114.5 MB}".
     * @param byteSize The size value in Byte.
     * @return The formatted string.
     */
    public static String getFormattedSizeString(long byteSize) {
        if (byteSize == 0)
            return "0";
        long absByteSize = Math.abs(byteSize);
        for (Long unitSize : sizeMap.keySet()) {
            if (unitSize <= absByteSize && absByteSize < unitSize << 10) {
                return (byteSize < 0 ? "-" : "") + sizeFormat.format((double) byteSize / unitSize)
                        + ' ' + sizeMap.get(unitSize);
            }
        }
        return "N/A";
    }

    /** Gets a masked URL string whose protocol, host, and path of the URL are preserved,
     * while the query string is masked if it exists.
     * @param url The URL object to be masked.
     * @return The masked URL.
     */
    public static String getMaskedURL(URL url) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol());
        sb.append("://");
        sb.append(url.getHost());
        sb.append(url.getPath());
        if (url.getQuery() != null)
            sb.append("?***");
        return sb.toString();
    }

    /** Gets a related time string.
     * @param instant The time instant.
     * @return The formatted string.
     */
    public static String getRelatedTimeString(Instant instant) {
        if (instant == null || instant.toEpochMilli() <= 0)
            return "N/A";

        Instant now = Instant.now();
        boolean isFuture = instant.isAfter(now);

        long seconds = Math.abs(between(now, instant).getSeconds());
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;

        if (months > 12)
            return isFuture ? "很久以后" : "很久以前";
        else if (months >= 1)
            return months + (isFuture ? " 个月后" : " 个月前");
        else if (days >= 1)
            return days + (isFuture ? " 天后" : " 天前");
        else if (hours >= 1)
            return hours + (isFuture ? " 小时后" : " 小时前");
        else if (minutes >= 1)
            return minutes + (isFuture ? " 分钟后" : " 分钟前");
        else if (seconds >= 1)
            return isFuture ? "即将" : "刚刚";
        else
            return "现在";
    }

    /** Gets a human-readable time string, in {@link #simpleDateTimeFormat} format.
     * @param instant The time instant.
     * @return The formatted string.
     */
    public static String getSimpleTimeString(Instant instant) {
        if (instant == null || instant.toEpochMilli() <= 0)
            return "N/A";

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return simpleDateTimeFormat.format(localDateTime);
    }

    /** Counts the number of matches of a given regular expression in the input string.
     * @param input The input string in which matches are to be counted.
     * @param regex The regular expression pattern to match against the input string.
     * @return The number of non-overlapping matches found in the input string.
     */
    public static int countMatches(String input, String regex) {
        if (input == null || regex == null || input.isEmpty() || regex.isEmpty())
            return 0;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find() && matcher.start() != matcher.end())
            count++;
        return count;
    }


    public static class URLStringBuilder implements CharSequence {
        private static final int MAX_LENGTH = 4096;
        private static final int MAX_QUERY_COUNT = 2048;

        private final String url;
        private final LinkedHashMap<String, String> params;
        private String buildCache;

        /** Initializes a builder for URL string building.
         * @param baseUrl The base URL string.
         */
        public URLStringBuilder(String baseUrl) {
            if (baseUrl == null || baseUrl.isEmpty())
                throw new IllegalArgumentException("Base URL cannot be null or empty");
            url = baseUrl;
            params = new LinkedHashMap<>(8);
            buildCache = null;
        }

        /** Adds a query parameter to the URL string being built.
         * @param key The query parameter key, cannot be {@code null} or empty.
         * @param value The query parameter value, can be {@code null} or empty.
         * @return The current {@link URLStringBuilder} instance for method chaining.
         */
        public URLStringBuilder addQuery(String key, String value) {
            if (key == null || key.isEmpty())
                throw new IllegalArgumentException("Query key cannot be null or empty");
            params.put(key, value == null ? "" : value);
            buildCache = null;
            if (params.size() > MAX_QUERY_COUNT)
                throw new RuntimeException("Too many query parameters appended");
            return this;
        }

        /** Returns the constructed URL with query parameters appended if present.
         * @return The complete URL.
         */
        public URL toURL() {
            try {
                return new URL(toString());
            } catch (Exception e) {
                throw new RuntimeException("Invalid URL format: " + this, e);
            }
        }

        /** Returns the constructed URL string with query parameters appended if present.
         * @return The complete URL string.
         */
        @Override
        public String toString() {
            if (buildCache == null) {
                if (params.isEmpty()) {
                    buildCache = url;
                } else {
                    StringBuilder sb = new StringBuilder(url);
                    int i = 0;
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        sb.append(i++ == 0 ? '?' : '&');
                        sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                        sb.append('=');
                        sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                    }
                    buildCache = sb.toString();
                }
            }
            if (buildCache.length() > MAX_LENGTH)
                throw new RuntimeException("Constructed URL exceeds max length limit");
            return buildCache;
        }

        @Override
        public int length() {
            return toString().length();
        }

        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        @Override
        public boolean isEmpty() {
            return toString().isEmpty();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }

        @Override
        public IntStream chars() {
            return toString().codePoints();
        }

        @Override
        public IntStream codePoints() {
            return toString().codePoints();
        }
    }
}
