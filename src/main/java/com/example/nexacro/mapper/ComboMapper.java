package com.example.nexacro.mapper;

import com.example.nexacro.dto.CodeMst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ComboMapper {
    List<CodeMst> selectByGroupCds(@Param("groupCds") List<String> groupCds);
}
