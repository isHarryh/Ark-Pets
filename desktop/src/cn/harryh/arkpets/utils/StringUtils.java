/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
