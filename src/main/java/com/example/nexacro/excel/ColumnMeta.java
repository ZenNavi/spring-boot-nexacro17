package com.example.nexacro.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMeta {
    private String colId;          // Dataset 컬럼명 (DB alias와 매핑)
    private String headerText;     // 헤더 표시 텍스트
    private int colWidth;          // 컬럼 너비 (px)
    private String textAlign;      // left / center / right
    private String colType;        // text / number / date
    private String numberFormat;   // #,##0 / #,##0.00 (colType=number)
    private String dateFormat;     // yyyy-MM-dd (colType=date)
    private String editType;       // text / combo
    private String comboGroupCd;   // CODE_MST.GROUP_CD (editType=combo)
    private String comboCodeCol;   // 기본값: CODE
    private String comboTextCol;   // 기본값: CODE_NM
    private String bandId;         // 소속 밴드 ID (null이면 전체 헤더 높이에 rowspan)
    private String bgColor;        // HEX (#FFFFFF), null이면 기본색
    private boolean fontBold;
    private int fontSize;          // 0이면 기본 크기
    private String borderStyle;    // thin / medium / thick / none
}
