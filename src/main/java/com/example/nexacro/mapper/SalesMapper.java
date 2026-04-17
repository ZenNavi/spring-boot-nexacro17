package com.example.nexacro.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ResultHandler;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper {
    List<Map<String, Object>> selectAll();
    void selectAllWithHandler(ResultHandler<Map<String, Object>> handler);
}
