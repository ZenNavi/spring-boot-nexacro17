package com.example.nexacro.excel;

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
