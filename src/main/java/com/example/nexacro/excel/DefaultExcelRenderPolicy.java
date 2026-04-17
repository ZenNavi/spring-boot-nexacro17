package com.example.nexacro.excel;

import com.example.nexacro.util.DateParseUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

        String text = String.valueOf(rawValue);
        try {
            return DateParseUtil.format(text, outputPattern);
        } catch (IllegalArgumentException ignored) {
            return rawValue;
        }
    }
}
