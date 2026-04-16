package com.example.nexacro.util;

import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.util.regex.Pattern;

/**
 * Utility for parsing various date/time string formats into Java time objects.
 *
 * Supported input formats:
 *   20260227                   → yyyyMMdd
 *   0930                       → HHmm
 *   09:30                      → HH:mm
 *   2205-02-27                 → yyyy-MM-dd
 *   2202-01-30 12:12:12        → yyyy-MM-dd HH:mm:ss
 *   2206-02-27 12:12:12.12345  → yyyy-MM-dd HH:mm:ss.S (fractional seconds)
 *   2206.02.27                 → yyyy.MM.dd
 */
public class DateParseUtil {

    public enum DateType { DATE, TIME, DATETIME }

    // ---------------------------------------------------------------------------
    // ParseResult
    // ---------------------------------------------------------------------------

    public record ParseResult(DateType type, LocalDate date, LocalTime time) {

        public static ParseResult ofDate(LocalDate d) {
            return new ParseResult(DateType.DATE, d, null);
        }

        public static ParseResult ofTime(LocalTime t) {
            return new ParseResult(DateType.TIME, null, t);
        }

        public static ParseResult ofDateTime(LocalDate d, LocalTime t) {
            return new ParseResult(DateType.DATETIME, d, t);
        }

        /** Convert to LocalDateTime. TIME-only uses today's date; DATE-only uses midnight. */
        public LocalDateTime toLocalDateTime() {
            return switch (type) {
                case DATE     -> date.atStartOfDay();
                case TIME     -> LocalDate.now().atTime(time);
                case DATETIME -> LocalDateTime.of(date, time);
            };
        }

        /** Extract LocalDate. TIME-only returns today. */
        public LocalDate toLocalDate() {
            return switch (type) {
                case DATE, DATETIME -> date;
                case TIME           -> LocalDate.now();
            };
        }

        /** Extract LocalTime. DATE-only returns midnight. */
        public LocalTime toLocalTime() {
            return switch (type) {
                case TIME, DATETIME -> time;
                case DATE           -> LocalTime.MIDNIGHT;
            };
        }

        /** Format using a pattern string (e.g. "yyyy/MM/dd HH:mm:ss"). */
        public String format(String pattern) {
            return toLocalDateTime().format(DateTimeFormatter.ofPattern(pattern));
        }

        /** Format using a DateTimeFormatter. */
        public String format(DateTimeFormatter formatter) {
            return toLocalDateTime().format(formatter);
        }
    }

    // ---------------------------------------------------------------------------
    // Regex patterns  (ordered from most-specific to least-specific)
    // ---------------------------------------------------------------------------

    private static final Pattern P_DATETIME_FRAC  =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+$");
    private static final Pattern P_DATETIME_DASH  =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern P_DATE_COMPACT   = Pattern.compile("^\\d{8}$");
    private static final Pattern P_DATE_DASH      = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern P_DATE_DOT       = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}$");
    private static final Pattern P_TIME_COMPACT   = Pattern.compile("^\\d{4}$");
    private static final Pattern P_TIME_COLON     = Pattern.compile("^\\d{2}:\\d{2}$");

    // ---------------------------------------------------------------------------
    // Formatters
    // ---------------------------------------------------------------------------

    private static final DateTimeFormatter FMT_DATE_COMPACT  =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FMT_TIME_COMPACT  =
            DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter FMT_TIME_COLON    =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DATE_DASH     =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_DATETIME_DASH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_DATE_DOT      =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    // Supports 1–9 fractional-second digits
    private static final DateTimeFormatter FMT_DATETIME_FRAC =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                    .toFormatter();

    // ---------------------------------------------------------------------------
    // Core parse
    // ---------------------------------------------------------------------------

    /**
     * Parse a date/time string into a {@link ParseResult}.
     *
     * @param input date/time string in any supported format
     * @return ParseResult containing DateType and parsed values
     * @throws IllegalArgumentException if the format is not recognized
     */
    public static ParseResult parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input is null or empty");
        }
        String s = input.trim();

        if (P_DATETIME_FRAC.matcher(s).matches()) {
            LocalDateTime ldt = LocalDateTime.parse(s, FMT_DATETIME_FRAC);
            return ParseResult.ofDateTime(ldt.toLocalDate(), ldt.toLocalTime());
        }
        if (P_DATETIME_DASH.matcher(s).matches()) {
            LocalDateTime ldt = LocalDateTime.parse(s, FMT_DATETIME_DASH);
            return ParseResult.ofDateTime(ldt.toLocalDate(), ldt.toLocalTime());
        }
        if (P_DATE_COMPACT.matcher(s).matches()) {
            return ParseResult.ofDate(LocalDate.parse(s, FMT_DATE_COMPACT));
        }
        if (P_DATE_DASH.matcher(s).matches()) {
            return ParseResult.ofDate(LocalDate.parse(s, FMT_DATE_DASH));
        }
        if (P_DATE_DOT.matcher(s).matches()) {
            return ParseResult.ofDate(LocalDate.parse(s, FMT_DATE_DOT));
        }
        if (P_TIME_COMPACT.matcher(s).matches()) {
            return ParseResult.ofTime(LocalTime.parse(s, FMT_TIME_COMPACT));
        }
        if (P_TIME_COLON.matcher(s).matches()) {
            return ParseResult.ofTime(LocalTime.parse(s, FMT_TIME_COLON));
        }

        throw new IllegalArgumentException("Unsupported date format: [" + input + "]");
    }

    // ---------------------------------------------------------------------------
    // Convenience shortcuts
    // ---------------------------------------------------------------------------

    public static LocalDate toLocalDate(String input) {
        return parse(input).toLocalDate();
    }

    public static LocalTime toLocalTime(String input) {
        return parse(input).toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(String input) {
        return parse(input).toLocalDateTime();
    }

    /**
     * Parse input and reformat to the given pattern.
     * Example: format("20260227", "yyyy/MM/dd") → "2026/02/27"
     */
    public static String format(String input, String pattern) {
        return parse(input).format(pattern);
    }

    public static String format(String input, DateTimeFormatter formatter) {
        return parse(input).format(formatter);
    }
}
