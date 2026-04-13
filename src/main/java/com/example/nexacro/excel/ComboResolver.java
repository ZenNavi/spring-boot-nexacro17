package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComboResolver {

    // Map<groupCd, Map<code, codeNm>>
    private final Map<String, Map<String, String>> comboMap;

    public ComboResolver(List<CodeMst> codeList) {
        comboMap = new HashMap<>();
        if (codeList == null) return;
        for (CodeMst item : codeList) {
            comboMap
                .computeIfAbsent(item.getGroupCd(), k -> new LinkedHashMap<>())
                .put(item.getCode(), item.getCodeNm());
        }
    }

    /**
     * groupCd + codeValue로 표시 텍스트 반환.
     * 매핑 없으면 codeValue 그대로 반환 (graceful fallback).
     */
    public String resolve(String groupCd, String codeValue) {
        if (codeValue == null) return null;
        if (groupCd == null) return codeValue;
        Map<String, String> codeMap = comboMap.get(groupCd);
        if (codeMap == null) return codeValue;
        return codeMap.getOrDefault(codeValue, codeValue);
    }
}
