package com.example.nexacro.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Pattern;

/**
 * Utility for parsing various date/time string formats into Java time objects.
 *
 * Supported input formats:
 *   20260227                   -> yyyyMMdd
 *   0930                       -> HHmm
 *   09:30                      -> HH:mm
 *   09:30:45                   -> HH:mm:ss
 *   2205-02-27                 -> yyyy-MM-dd
 *   2205-02-27 12:12           -> yyyy-MM-dd HH:mm
 *   2202-01-30 12:12:12        -> yyyy-MM-dd HH:mm:ss
 *   2206-02-27 12:12:12.12345  -> yyyy-MM-dd HH:mm:ss.S (fractional seconds)
 *   2206.02.27                 -> yyyy.MM.dd
 */
public class DateParseUtil {

    public enum DateType { DATE, TIME, DATETIME }

    public static class ParseResult {
        private final DateType type;
        private final LocalDate date;
        private final LocalTime time;

        private ParseResult(DateType type, LocalDate date, LocalTime time) {
            this.type = type;
            this.date = date;
            this.time = time;
        }

        public static ParseResult ofDate(LocalDate date) {
            return new ParseResult(DateType.DATE, date, null);
        }

        public static ParseResult ofTime(LocalTime time) {
            return new ParseResult(DateType.TIME, null, time);
        }

        public static ParseResult ofDateTime(LocalDate date, LocalTime time) {
            return new ParseResult(DateType.DATETIME, date, time);
        }

        public DateType type() {
            return type;
        }

        public LocalDate date() {
            return date;
        }

        public LocalTime time() {
            return time;
        }

        public LocalDateTime toLocalDateTime() {
            if (type == DateType.DATE) {
                return date.atStartOfDay();
            }
            if (type == DateType.TIME) {
                return LocalDate.now().atTime(time);
            }
            return LocalDateTime.of(date, time);
        }

        public LocalDate toLocalDate() {
            if (type == DateType.TIME) {
                return LocalDate.now();
            }
            return date;
        }

        public LocalTime toLocalTime() {
            if (type == DateType.DATE) {
                return LocalTime.MIDNIGHT;
            }
            return time;
        }

        public String format(String pattern) {
            return toLocalDateTime().format(DateTimeFormatter.ofPattern(pattern));
        }

        public String format(DateTimeFormatter formatter) {
            return toLocalDateTime().format(formatter);
        }
    }

    private static final Pattern P_DATETIME_FRAC =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+$");
    private static final Pattern P_DATETIME_MINUTE =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");
    private static final Pattern P_DATETIME_DASH =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern P_DATE_COMPACT = Pattern.compile("^\\d{8}$");
    private static final Pattern P_DATE_DASH = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern P_DATE_DOT = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}$");
    private static final Pattern P_TIME_COMPACT = Pattern.compile("^\\d{4}$");
    private static final Pattern P_TIME_COLON_SEC = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern P_TIME_COLON = Pattern.compile("^\\d{2}:\\d{2}$");

    private static final DateTimeFormatter FMT_DATE_COMPACT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FMT_TIME_COMPACT =
            DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter FMT_TIME_COLON =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_TIME_COLON_SEC =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_DATE_DASH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_DATETIME_MINUTE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_DATETIME_DASH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_DATE_DOT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter FMT_DATETIME_FRAC =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                    .toFormatter();

    public static ParseResult parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input is null or empty");
        }
        String value = input.trim();

        if (P_DATETIME_FRAC.matcher(value).matches()) {
            LocalDateTime dateTime = LocalDateTime.parse(value, FMT_DATETIME_FRAC);
            return ParseResult.ofDateTime(dateTime.toLocalDate(), dateTime.toLocalTime());
        }
        if (P_DATETIME_MINUTE.matcher(value).matches()) {
            LocalDateTime dateTime = LocalDateTime.parse(value, FMT_DATETIME_MINUTE);
            return ParseResult.ofDateTime(dateTime.toLocalDate(), dateTime.toLocalTime());
        }
        if (P_DATETIME_DASH.matcher(value).matches()) {
            LocalDateTime dateTime = LocalDateTime.parse(value, FMT_DATETIME_DASH);
            return ParseResult.ofDateTime(dateTime.toLocalDate(), dateTime.toLocalTime());
        }
        if (P_DATE_COMPACT.matcher(value).matches()) {
            return ParseResult.ofDate(LocalDate.parse(value, FMT_DATE_COMPACT));
        }
        if (P_DATE_DASH.matcher(value).matches()) {
            return ParseResult.ofDate(LocalDate.parse(value, FMT_DATE_DASH));
        }
        if (P_DATE_DOT.matcher(value).matches()) {
            return ParseResult.ofDate(LocalDate.parse(value, FMT_DATE_DOT));
        }
        if (P_TIME_COMPACT.matcher(value).matches()) {
            return ParseResult.ofTime(LocalTime.parse(value, FMT_TIME_COMPACT));
        }
        if (P_TIME_COLON_SEC.matcher(value).matches()) {
            return ParseResult.ofTime(LocalTime.parse(value, FMT_TIME_COLON_SEC));
        }
        if (P_TIME_COLON.matcher(value).matches()) {
            return ParseResult.ofTime(LocalTime.parse(value, FMT_TIME_COLON));
        }

        throw new IllegalArgumentException("Unsupported date format: [" + input + "]");
    }

    public static LocalDate toLocalDate(String input) {
        return parse(input).toLocalDate();
    }

    public static LocalTime toLocalTime(String input) {
        return parse(input).toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(String input) {
        return parse(input).toLocalDateTime();
    }

    public static String format(String input, String pattern) {
        return parse(input).format(pattern);
    }

    public static String format(String input, DateTimeFormatter formatter) {
        return parse(input).format(formatter);
    }

    public static String format(String input, String inputPattern, String outputPattern) {
        return format(input,
                DateTimeFormatter.ofPattern(inputPattern),
                DateTimeFormatter.ofPattern(outputPattern));
    }

    public static String format(String input, DateTimeFormatter inputFormatter, String outputPattern) {
        return format(input, inputFormatter, DateTimeFormatter.ofPattern(outputPattern));
    }

    public static String format(String input, String inputPattern, DateTimeFormatter outputFormatter) {
        return format(input, DateTimeFormatter.ofPattern(inputPattern), outputFormatter);
    }

    public static String format(String input, DateTimeFormatter inputFormatter, DateTimeFormatter outputFormatter) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input is null or empty");
        }

        TemporalAccessor parsed = inputFormatter.parseBest(
                input.trim(),
                LocalDateTime::from,
                LocalDate::from,
                LocalTime::from
        );

        if (parsed instanceof LocalDateTime) {
            return outputFormatter.format((LocalDateTime) parsed);
        }
        if (parsed instanceof LocalDate) {
            return outputFormatter.format((LocalDate) parsed);
        }
        if (parsed instanceof LocalTime) {
            return outputFormatter.format((LocalTime) parsed);
        }

        throw new IllegalArgumentException("Unsupported temporal value: [" + input + "]");
    }
}
