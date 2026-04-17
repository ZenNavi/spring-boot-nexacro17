package com.example.nexacro.excel;

/**
 * 셀 스타일 override 값을 담는 DTO다.
 *
 * <p>기본 스타일은 {@link ColumnMeta}를 기준으로 만들고, 이 객체에 값이 있으면
 * 해당 속성만 덮어쓴다. 즉 서비스별 정책에서 "필요한 속성만" 바꿀 수 있다.</p>
 */
public class ExcelCellStyleSpec {
    private String textAlign;
    private String backgroundColor;
    private String numberFormat;
    private String borderStyle;
    private Boolean fontBold;
    private Integer fontSize;

    public static ExcelCellStyleSpec empty() {
        return new ExcelCellStyleSpec();
    }

    public String getTextAlign() {
        return textAlign;
    }

    public ExcelCellStyleSpec setTextAlign(String textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public ExcelCellStyleSpec setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public ExcelCellStyleSpec setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
        return this;
    }

    public String getBorderStyle() {
        return borderStyle;
    }

    public ExcelCellStyleSpec setBorderStyle(String borderStyle) {
        this.borderStyle = borderStyle;
        return this;
    }

    public Boolean getFontBold() {
        return fontBold;
    }

    public ExcelCellStyleSpec setFontBold(Boolean fontBold) {
        this.fontBold = fontBold;
        return this;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public ExcelCellStyleSpec setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }
}
