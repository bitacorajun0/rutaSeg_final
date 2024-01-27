package com.jumani.rutaseg.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface DateGen {

    default ZonedDateTime currentDateUTC() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }
}
