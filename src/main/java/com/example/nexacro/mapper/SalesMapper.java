package com.example.nexacro.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper {
    List<Map<String, Object>> selectAll();
}
