package com.example.nexacro.dto;

public class CodeMst {
    private String groupCd;
    private String code;
    private String codeNm;
    private int sortOrder;

    // Explicit getters/setters as fallback for environments where Lombok APT is unavailable
    public String getGroupCd() { return groupCd; }
    public void setGroupCd(String groupCd) { this.groupCd = groupCd; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCodeNm() { return codeNm; }
    public void setCodeNm(String codeNm) { this.codeNm = codeNm; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
