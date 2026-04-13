package com.example.nexacro.excel;

public class ColumnMeta {
    private String colId;
    private String headerText;
    private int colWidth;
    private String textAlign;
    private String colType;
    private String numberFormat;
    private String dateFormat;
    private String editType;
    private String comboGroupCd;
    private String comboCodeCol;
    private String comboTextCol;
    private String bandId;
    private String bgColor;
    private boolean fontBold;
    private int fontSize;
    private String borderStyle;

    public ColumnMeta() {}

    private ColumnMeta(Builder b) {
        this.colId = b.colId; this.headerText = b.headerText; this.colWidth = b.colWidth;
        this.textAlign = b.textAlign; this.colType = b.colType; this.numberFormat = b.numberFormat;
        this.dateFormat = b.dateFormat; this.editType = b.editType; this.comboGroupCd = b.comboGroupCd;
        this.comboCodeCol = b.comboCodeCol; this.comboTextCol = b.comboTextCol; this.bandId = b.bandId;
        this.bgColor = b.bgColor; this.fontBold = b.fontBold; this.fontSize = b.fontSize;
        this.borderStyle = b.borderStyle;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String colId, headerText, textAlign, colType, numberFormat, dateFormat,
                       editType, comboGroupCd, comboCodeCol, comboTextCol, bandId, bgColor, borderStyle;
        private int colWidth, fontSize;
        private boolean fontBold;

        public Builder colId(String v)          { this.colId = v; return this; }
        public Builder headerText(String v)     { this.headerText = v; return this; }
        public Builder colWidth(int v)          { this.colWidth = v; return this; }
        public Builder textAlign(String v)      { this.textAlign = v; return this; }
        public Builder colType(String v)        { this.colType = v; return this; }
        public Builder numberFormat(String v)   { this.numberFormat = v; return this; }
        public Builder dateFormat(String v)     { this.dateFormat = v; return this; }
        public Builder editType(String v)       { this.editType = v; return this; }
        public Builder comboGroupCd(String v)   { this.comboGroupCd = v; return this; }
        public Builder comboCodeCol(String v)   { this.comboCodeCol = v; return this; }
        public Builder comboTextCol(String v)   { this.comboTextCol = v; return this; }
        public Builder bandId(String v)         { this.bandId = v; return this; }
        public Builder bgColor(String v)        { this.bgColor = v; return this; }
        public Builder fontBold(boolean v)      { this.fontBold = v; return this; }
        public Builder fontSize(int v)          { this.fontSize = v; return this; }
        public Builder borderStyle(String v)    { this.borderStyle = v; return this; }
        public ColumnMeta build()               { return new ColumnMeta(this); }
    }

    // Getters & Setters
    public String getColId()          { return colId; }
    public void setColId(String v)    { this.colId = v; }
    public String getHeaderText()     { return headerText; }
    public void setHeaderText(String v) { this.headerText = v; }
    public int getColWidth()          { return colWidth; }
    public void setColWidth(int v)    { this.colWidth = v; }
    public String getTextAlign()      { return textAlign; }
    public void setTextAlign(String v){ this.textAlign = v; }
    public String getColType()        { return colType; }
    public void setColType(String v)  { this.colType = v; }
    public String getNumberFormat()   { return numberFormat; }
    public void setNumberFormat(String v){ this.numberFormat = v; }
    public String getDateFormat()     { return dateFormat; }
    public void setDateFormat(String v){ this.dateFormat = v; }
    public String getEditType()       { return editType; }
    public void setEditType(String v) { this.editType = v; }
    public String getComboGroupCd()   { return comboGroupCd; }
    public void setComboGroupCd(String v){ this.comboGroupCd = v; }
    public String getComboCodeCol()   { return comboCodeCol; }
    public void setComboCodeCol(String v){ this.comboCodeCol = v; }
    public String getComboTextCol()   { return comboTextCol; }
    public void setComboTextCol(String v){ this.comboTextCol = v; }
    public String getBandId()         { return bandId; }
    public void setBandId(String v)   { this.bandId = v; }
    public String getBgColor()        { return bgColor; }
    public void setBgColor(String v)  { this.bgColor = v; }
    public boolean isFontBold()       { return fontBold; }
    public void setFontBold(boolean v){ this.fontBold = v; }
    public int getFontSize()          { return fontSize; }
    public void setFontSize(int v)    { this.fontSize = v; }
    public String getBorderStyle()    { return borderStyle; }
    public void setBorderStyle(String v){ this.borderStyle = v; }
}
