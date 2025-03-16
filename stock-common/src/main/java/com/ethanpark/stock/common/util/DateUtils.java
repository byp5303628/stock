package com.ethanpark.stock.common.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@Slf4j
public class DateUtils {
    public static final String DEFAULT_TIMEZONE = "GMT+08:00";

    /**
     * date按格式转为String
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        return dateFormat.format(date);
    }

    public static String formatDate(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * String类型装成Date
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static LocalDateTime parseStringToDate(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return localDate.atStartOfDay();
    }

    public static String plusDay(String partitionDate) {
        return plusDay(partitionDate, 1);
    }

    public static String plusDay(String partitionDate, int count) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(partitionDate);
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.DAY_OF_MONTH, count);

            return new SimpleDateFormat("yyyy-MM-dd").format(instance.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String plusYear(String partitionDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(partitionDate);
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.YEAR, 1);

            return new SimpleDateFormat("yyyy-MM-dd").format(instance.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getWeekOfYear(String partitionDate) {
        LocalDateTime date = parseStringToDate(partitionDate, "yyyy-MM-dd");

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    /**
     * 计算两个日期之间的精确月份差（包括小数部分）
     *
     * @param beginDate 第一个日期 (格式: yyyy-MM-dd)
     * @param endDate   第二个日期 (格式: yyyy-MM-dd)
     * @return 精确的月份差
     */
    public static double monthDiff(String beginDate, String endDate) {
        if (StringUtils.isEmpty(beginDate) || StringUtils.isEmpty(endDate)) {
            return 0D;
        }

        LocalDate localDate1 = LocalDate.parse(beginDate);
        LocalDate localDate2 = LocalDate.parse(endDate);

        long days = Math.abs(localDate1.toEpochDay() - localDate2.toEpochDay());
        double months = days / 30.436875; // 平均每月的天数

        return Math.round(months * 100.0) / 100.0; // 四舍五入到两位小数
    }

    /**
     * 计算两个日期之间的精确年份差（包括小数部分）
     *
     * @param beginDate 第一个日期 (格式: yyyy-MM-dd)
     * @param endDate   第二个日期 (格式: yyyy-MM-dd)
     * @return 精确的年份差
     */
    public static double yearDiff(String beginDate, String endDate) {
        if (StringUtils.isEmpty(beginDate) || StringUtils.isEmpty(endDate)) {
            return 0D;
        }

        LocalDate localDate1 = LocalDate.parse(beginDate);
        LocalDate localDate2 = LocalDate.parse(endDate);

        long days = Math.abs(localDate1.toEpochDay() - localDate2.toEpochDay());
        double years = days / 365.25; // 考虑闰年的平均每年天数

        return Math.round(years * 100.0) / 100.0; // 四舍五入到两位小数
    }

    public static int dayDiff(String beginDate, String endDate) {
        LocalDate localDate1 = LocalDate.parse(beginDate);
        LocalDate localDate2 = LocalDate.parse(endDate);

        long days = Math.abs(localDate1.toEpochDay() - localDate2.toEpochDay());

        return (int) days;
    }

    public static int getMonthOfYear(String partitionDate) {
        LocalDateTime date = parseStringToDate(partitionDate, "yyyy-MM-dd");

        return date.getMonthValue();
    }
}
