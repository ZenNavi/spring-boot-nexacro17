package com.example.nexacro.excel;

import com.example.nexacro.util.DateParseUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 기본 Excel 렌더링 정책이다.
 *
 * <p>별도 정책을 주입하지 않으면 다음 공통 규칙을 수행한다.</p>
 * <ul>
 *   <li>combo 컬럼은 code 값을 name으로 치환</li>
 *   <li>dateFormat이 지정된 컬럼은 문자열 날짜를 해당 포맷으로 변환</li>
 * </ul>
 */
@Component
public class DefaultExcelRenderPolicy implements ExcelRenderPolicy {

    @Override
    public Object resolveCellValue(ExcelCellContext context) {
        ColumnMeta column = context.getColumnMeta();
        Object rawValue = context.getRawValue();

        if (rawValue == null) {
            return null;
        }

        if ("combo".equals(column.getEditType())) {
            return context.getComboResolver().resolve(column.getComboGroupCd(), String.valueOf(rawValue));
        }

        if (column.getDateFormat() != null && !column.getDateFormat().trim().isEmpty()) {
            return formatDateValue(rawValue, column.getDateFormat());
        }

        return rawValue;
    }

    private Object formatDateValue(Object rawValue, String outputPattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputPattern);

        // 이미 날짜/시간 타입인 경우는 그대로 포맷한다.
        if (rawValue instanceof LocalDateTime) {
            return formatter.format((LocalDateTime) rawValue);
        }
        if (rawValue instanceof LocalDate) {
            return formatter.format((LocalDate) rawValue);
        }
        if (rawValue instanceof LocalTime) {
            return formatter.format((LocalTime) rawValue);
        }
        if (rawValue instanceof Date) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(((Date) rawValue).toInstant(), ZoneId.systemDefault());
            return formatter.format(dateTime);
        }

        // 문자열인 경우 DateParseUtil이 인식 가능한 포맷이면 문자 포맷으로 변환한다.
        String text = String.valueOf(rawValue);
        try {
            return DateParseUtil.format(text, outputPattern);
        } catch (IllegalArgumentException ignored) {
            return rawValue;
        }
    }
}
