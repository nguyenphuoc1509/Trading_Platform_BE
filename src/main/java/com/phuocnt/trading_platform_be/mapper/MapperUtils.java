package com.phuocnt.trading_platform_be.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MapperUtils {
    // LocalDateTime → Unix milliseconds
    public static Long toMillis(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // BigDecimal → String 8
    public static String fmt8(BigDecimal val) {
        if (val == null) return null;
        return val.setScale(8, RoundingMode.HALF_UP).toPlainString();
    }

    // BigDecimal → String 4
    public static String fmt4(BigDecimal val) {
        if (val == null) return null;
        return val.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    // BigDecimal → String 2
    public static String fmt2(BigDecimal val) {
        if (val == null) return null;
        return val.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // Double → String 8
    public static String fmtDouble8(Double val) {
        if (val == null) return null;
        return BigDecimal.valueOf(val).setScale(8, RoundingMode.HALF_UP).toPlainString();
    }

    // Double → String 4
    public static String fmtDouble4(Double val) {
        if (val == null) return null;
        return BigDecimal.valueOf(val).setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}
