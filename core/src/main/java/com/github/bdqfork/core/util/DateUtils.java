package com.github.bdqfork.core.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author bdq
 * @since 2020/11/6
 */
public class DateUtils {
    public static Date getDateFromNow(long offset, ChronoUnit unit) {
        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.plus(offset, unit);
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static String getNow(String pattern) {
        LocalDateTime dateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
