package com.xz.scorep.executor.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * double 工具类
 *
 * @author zhaorenwu
 */
public class DoubleUtils {

    private static DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static DecimalFormat FORMAT_PECISION = new DecimalFormat("0.000000");

    public static String toPercent(double value) {
        if (!isValidNumber(value)) {
            return "0%";
        }
        if (value < 0.0001d && value > 0) {
            return FORMAT_PECISION.format(value * 100) + "%";
        } else {
            return FORMAT.format(value * 100) + "%";
        }
    }

    /**
     * double 保留两位小数
     *
     * @param f 原数值
     * @return 保留两位小数的数值
     */
    public static Double round(double f) {
        return round(f, false);
    }

    /**
     * double 保留两位或四位小数
     *
     * @param doubleValue 原数值
     * @param percent     是否是百分比，如果为 true 则返回四位小数
     * @return 保留X位小数的数值
     */
    public static Double round(double doubleValue, boolean percent) {

        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
            return 0d;
        }

        BigDecimal b = new BigDecimal(doubleValue);
        return b.setScale(percent ? 4 : 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static boolean isValidNumber(double d) {
        return !(
                d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY || Double.isNaN(d)
        );
    }

    /**
     * double 如果小数点后为零显示整数否则保留精度
     *
     * @param num 原数值
     * @return 保留X位小数的数值
     */
    public static String cutTailZero(double num) {
        if (num % 1.0 == 0) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }
}
