package com.example.nexacro.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandMeta {
    private String bandId;      // 밴드 고유 ID (ColumnMeta.bandId와 연결)
    private String bandText;    // 밴드 헤더 표시 텍스트
    private int colSpan;        // 가로 병합 컬럼 수
    private int rowSpan;        // 세로 병합 행 수 (보통 1)
    private int bandOrder;      // 밴드 표시 순서 (오름차순)
    private String bgColor;     // HEX (#D9E1F2)
    private String textAlign;   // left / center / right
    private boolean fontBold;
}
