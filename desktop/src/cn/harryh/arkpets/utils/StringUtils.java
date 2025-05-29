/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import java.text.DecimalFormat;
import java.util.Map;


public class StringUtils {
    private static final Map<Long, String> sizeMap = Map.of(
            1L, "B",
            1L << 10, "KB",
            1L << 20, "MB",
            1L << 30, "GB",
            1L << 40, "TB"
    );
    private static final DecimalFormat sizeFormat = new DecimalFormat("0.0");


    /** Gets a formatted size string, e.g."{@code 114.5 MB}".
     * @param byteSize The size value in Byte.
     * @return The formatted string.
     */
    public static String getFormattedSizeString(long byteSize) {
        if (byteSize == 0)
            return "0";
        for (Long unitSize : sizeMap.keySet()) {
            if (unitSize <= byteSize && byteSize < unitSize << 10)
                return sizeFormat.format((double) byteSize / unitSize) + " " + sizeMap.get(unitSize);
        }
        return "未知";
    }
}
