package com.example.nexacro.excel;

import java.util.Map;

/**
 * Excel row 스트리밍 처리 시, 한 건의 row 데이터를 전달받는 콜백이다.
 *
 * <p>대량 조회 시 결과 전체를 List로 메모리에 올리지 않고, row 단위로
 * builder에 흘려보내기 위한 최소 단위 인터페이스다.</p>
 */
public interface ExcelRowConsumer {
    void accept(Map<String, Object> rowData) throws Exception;
}
