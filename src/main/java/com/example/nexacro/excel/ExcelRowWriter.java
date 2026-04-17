package com.example.nexacro.excel;

import java.util.List;
import java.util.Map;

/**
 * Excel builder에 데이터를 공급하는 추상화 인터페이스다.
 *
 * <p>호출 측은 이 인터페이스를 통해 다음 두 방식을 모두 사용할 수 있다.</p>
 * <ul>
 *   <li>이미 준비된 {@code List<Map<String, Object>>}를 그대로 전달</li>
 *   <li>MyBatis {@code ResultHandler}처럼 row 단위로 순차 전달</li>
 * </ul>
 */
public interface ExcelRowWriter {

    void writeRows(ExcelRowConsumer consumer) throws Exception;

    /**
     * 기존 List 기반 호출 코드를 유지하기 위한 기본 어댑터다.
     */
    static ExcelRowWriter fromList(final List<Map<String, Object>> rows) {
        return new ExcelRowWriter() {
            @Override
            public void writeRows(ExcelRowConsumer consumer) throws Exception {
                for (Map<String, Object> row : rows) {
                    consumer.accept(row);
                }
            }
        };
    }
}
