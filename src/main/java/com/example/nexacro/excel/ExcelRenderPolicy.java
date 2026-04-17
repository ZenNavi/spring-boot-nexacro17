package com.example.nexacro.excel;

/**
 * Excel 셀 값/스타일을 서비스별로 주입하기 위한 확장 포인트다.
 *
 * <p>이 정책을 구현하면 다음을 서비스 단위로 제어할 수 있다.</p>
 * <ul>
 *   <li>raw data를 어떤 값으로 Excel에 출력할지</li>
 *   <li>셀 정렬, 배경색, 숫자 포맷 등을 어떻게 바꿀지</li>
 * </ul>
 */
public interface ExcelRenderPolicy {

    ExcelRenderPolicy NO_OP = new ExcelRenderPolicy() {
    };

    /**
     * raw value를 실제 Excel 셀에 기록할 값으로 변환한다.
     */
    default Object resolveCellValue(ExcelCellContext context) {
        return context.getRawValue();
    }

    /**
     * 기본 컬럼 스타일에서 override 할 속성을 반환한다.
     */
    default ExcelCellStyleSpec resolveCellStyle(ExcelCellContext context) {
        return ExcelCellStyleSpec.empty();
    }
}
