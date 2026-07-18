package apartmentmanagement.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public final class DateTimeUtil {


    public static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final TimeZone VN_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter TIME_HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_HMS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_HM = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter DATE_HMS = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FULL_SEC = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateTimeUtil() {
    }

    public static Timestamp nowTimestamp() {
        return Timestamp.from(Instant.now());
    }

    public static LocalDateTime nowLocalVn() {
        return LocalDateTime.now(VN_ZONE);
    }

    public static ZonedDateTime nowVn() {
        return ZonedDateTime.now(VN_ZONE);
    }

    public static String nowTimeHm() {
        return nowVn().format(TIME_HM);
    }

    public static String nowFull() {
        return nowVn().format(FULL);
    }


    public static String formatRealtime(Date date) {
        ZonedDateTime zdt = toVn(date);
        if (zdt == null) {
            return "";
        }
        LocalDate today = LocalDate.now(VN_ZONE);
        if (zdt.toLocalDate().equals(today)) {
            return zdt.format(TIME_HM);
        }
        return zdt.format(DATE_HM);
    }

    public static String formatRealtime(Timestamp ts) {
        return formatRealtime((Date) ts);
    }

    public static String formatHistory(Date date) {
        ZonedDateTime zdt = toVn(date);
        if (zdt == null) {
            return "";
        }
        LocalDate today = LocalDate.now(VN_ZONE);
        if (zdt.toLocalDate().equals(today)) {
            return zdt.format(TIME_HMS);
        }
        return zdt.format(DATE_HMS);
    }

    public static String formatHistory(Timestamp ts) {
        return formatHistory((Date) ts);
    }


    public static String formatChat(Timestamp ts) {
        if (ts == null) {
            return nowTimeHm();
        }
        return formatRealtime(ts);
    }

    public static String formatSentAt(Timestamp ts) {
        return formatTimeHm(ts);
    }

    public static String formatTimeHm(Date date) {
        ZonedDateTime zdt = toVn(date);
        return zdt == null ? "" : zdt.format(TIME_HM);
    }

    public static String formatTimeHm(Timestamp ts) {
        return formatTimeHm((Date) ts);
    }

    public static String formatFull(Date date) {
        ZonedDateTime zdt = toVn(date);
        return zdt == null ? "" : zdt.format(FULL);
    }

    public static String formatFull(Timestamp ts) {
        return formatFull((Date) ts);
    }


    public static String formatHistoryTitle(Date date) {
        ZonedDateTime zdt = toVn(date);
        return zdt == null ? "" : zdt.format(FULL_SEC);
    }

    public static String formatDateOnly(Date date) {
        ZonedDateTime zdt = toVn(date);
        return zdt == null ? "" : zdt.format(DATE_ONLY);
    }

    public static String formatTitle(Date date) {
        return formatFull(date);
    }

    private static ZonedDateTime toVn(Date date) {
        if (date == null) {
            return null;
        }
        try {
            Instant instant = Instant.ofEpochMilli(date.getTime());
            return instant.atZone(VN_ZONE);
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance(VN_TIMEZONE);
            cal.setTime(date);
            return ZonedDateTime.ofInstant(cal.toInstant(), VN_ZONE);
        }
    }
}
