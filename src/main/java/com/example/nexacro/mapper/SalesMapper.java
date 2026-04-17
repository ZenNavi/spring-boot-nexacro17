package com.example.nexacro.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ResultHandler;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper {

    /**
     * 전체 결과를 List로 조회한다.
     *
     * <p>기존 호출 호환 및 테스트/소량 데이터 처리용이다.</p>
     */
    List<Map<String, Object>> selectAll();

    /**
     * 전체 결과를 row 단위로 처리한다.
     *
     * <p>대량 Excel 생성 시 메모리 부담을 줄이기 위한 streaming 성격의 조회 방식이다.</p>
     */
    void selectAllWithHandler(ResultHandler<Map<String, Object>> handler);
}
