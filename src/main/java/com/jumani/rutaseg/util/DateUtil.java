package com.jumani.rutaseg.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateUtil {
    public static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

    private static final DateTimeFormatter DATE_TIME_FORMATTER_OUTPUT = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).appendOffsetId()
            .toFormatter();

    private static final DateTimeFormatter DATE_TIME_FORMATTER_INPUT = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).appendPattern("[XXX][X]")
            .toFormatter();

    private DateUtil() {
    }

    public static String zonedDateTimeToStringUTC(ZonedDateTime zonedDateTime) {
        return DATE_TIME_FORMATTER_OUTPUT.withZone(ZONE_ID_UTC).format(zonedDateTime);
    }

    public static ZonedDateTime stringToZonedDateTimeUTC(String dateString) {
        ZonedDateTime zdt = ZonedDateTime.parse(dateString, DATE_TIME_FORMATTER_INPUT);
        return zdt.withZoneSameInstant(ZONE_ID_UTC);
    }
}
