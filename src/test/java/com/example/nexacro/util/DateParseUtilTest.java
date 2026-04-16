package com.example.nexacro.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class DateParseUtilTest {

    // ---------------------------------------------------------------------------
    // DateType detection
    // ---------------------------------------------------------------------------

    @ParameterizedTest
    @DisplayName("DATE 타입 판별")
    @CsvSource({
        "20260227",
        "2205-02-27",
        "2206.02.27"
    })
    void shouldDetectDate(String input) {
        assertThat(DateParseUtil.parse(input).type())
                .isEqualTo(DateParseUtil.DateType.DATE);
    }

    @ParameterizedTest
    @DisplayName("TIME 타입 판별")
    @CsvSource({
        "0930",
        "09:30",
        "09:30:45"
    })
    void shouldDetectTime(String input) {
        assertThat(DateParseUtil.parse(input).type())
                .isEqualTo(DateParseUtil.DateType.TIME);
    }

    @ParameterizedTest
    @DisplayName("DATETIME 타입 판별")
    @CsvSource({
        "2205-02-27 12:12",
        "2202-01-30 12:12:12",
        "2206-02-27 12:12:12.12345"
    })
    void shouldDetectDateTime(String input) {
        assertThat(DateParseUtil.parse(input).type())
                .isEqualTo(DateParseUtil.DateType.DATETIME);
    }

    // ---------------------------------------------------------------------------
    // toLocalDate
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("yyyyMMdd → LocalDate")
    void parseDateCompact() {
        assertThat(DateParseUtil.toLocalDate("20260227"))
                .isEqualTo(LocalDate.of(2026, 2, 27));
    }

    @Test
    @DisplayName("yyyy-MM-dd → LocalDate")
    void parseDateDash() {
        assertThat(DateParseUtil.toLocalDate("2205-02-27"))
                .isEqualTo(LocalDate.of(2205, 2, 27));
    }

    @Test
    @DisplayName("yyyy.MM.dd → LocalDate")
    void parseDateDot() {
        assertThat(DateParseUtil.toLocalDate("2206.02.27"))
                .isEqualTo(LocalDate.of(2206, 2, 27));
    }

    // ---------------------------------------------------------------------------
    // toLocalTime
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("HHmm → LocalTime")
    void parseTimeCompact() {
        assertThat(DateParseUtil.toLocalTime("0930"))
                .isEqualTo(LocalTime.of(9, 30));
    }

    @Test
    @DisplayName("HH:mm → LocalTime")
    void parseTimeColon() {
        assertThat(DateParseUtil.toLocalTime("09:30"))
                .isEqualTo(LocalTime.of(9, 30));
    }

    @Test
    @DisplayName("HH:mm:ss → LocalTime")
    void parseTimeColonSecond() {
        assertThat(DateParseUtil.toLocalTime("09:30:45"))
                .isEqualTo(LocalTime.of(9, 30, 45));
    }

    // ---------------------------------------------------------------------------
    // toLocalDateTime
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("yyyy-MM-dd HH:mm → LocalDateTime")
    void parseDateTimeMinute() {
        assertThat(DateParseUtil.toLocalDateTime("2205-02-27 12:12"))
                .isEqualTo(LocalDateTime.of(2205, 2, 27, 12, 12));
    }

    @Test
    @DisplayName("yyyy-MM-dd HH:mm:ss → LocalDateTime")
    void parseDateTimeDash() {
        assertThat(DateParseUtil.toLocalDateTime("2202-01-30 12:12:12"))
                .isEqualTo(LocalDateTime.of(2202, 1, 30, 12, 12, 12));
    }

    @Test
    @DisplayName("yyyy-MM-dd HH:mm:ss.SSSSS → LocalDateTime (fractional seconds 무시)")
    void parseDateTimeFractional() {
        LocalDateTime result = DateParseUtil.toLocalDateTime("2206-02-27 12:12:12.12345");
        assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2206, 2, 27));
        assertThat(result.toLocalTime().getHour()).isEqualTo(12);
        assertThat(result.toLocalTime().getMinute()).isEqualTo(12);
        assertThat(result.toLocalTime().getSecond()).isEqualTo(12);
    }

    // ---------------------------------------------------------------------------
    // format convenience
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("format: 20260227 → yyyy/MM/dd")
    void formatDateCompact() {
        assertThat(DateParseUtil.format("20260227", "yyyy/MM/dd"))
                .isEqualTo("2026/02/27");
    }

    @Test
    @DisplayName("format: 2202-01-30 12:12:12 → yyyy년 MM월 dd일 HH시 mm분")
    void formatDateTimeKorean() {
        assertThat(DateParseUtil.format("2202-01-30 12:12:12", "yyyy년 MM월 dd일 HH시 mm분"))
                .isEqualTo("2202년 01월 30일 12시 12분");
    }

    @Test
    @DisplayName("format: inputPattern/outputPattern 명시 변환")
    void formatWithExplicitPatterns() {
        assertThat(DateParseUtil.format("20260227153045", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"))
                .isEqualTo("2026-02-27 15:30:45");
    }

    // ---------------------------------------------------------------------------
    // error handling
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("지원하지 않는 포맷 → IllegalArgumentException")
    void unsupportedFormat() {
        assertThatThrownBy(() -> DateParseUtil.parse("2026.02.27 12:12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported date format");
    }

    @Test
    @DisplayName("null 입력 → IllegalArgumentException")
    void nullInput() {
        assertThatThrownBy(() -> DateParseUtil.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
