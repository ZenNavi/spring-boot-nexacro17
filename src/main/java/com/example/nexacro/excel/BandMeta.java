package com.example.nexacro.excel;

public class BandMeta {
    private String bandId;
    private String bandText;
    private int colSpan;
    private int rowSpan;
    private int bandOrder;
    private String bgColor;
    private String textAlign;
    private boolean fontBold;

    public BandMeta() {}

    private BandMeta(Builder b) {
        this.bandId = b.bandId; this.bandText = b.bandText; this.colSpan = b.colSpan;
        this.rowSpan = b.rowSpan; this.bandOrder = b.bandOrder; this.bgColor = b.bgColor;
        this.textAlign = b.textAlign; this.fontBold = b.fontBold;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String bandId, bandText, bgColor, textAlign;
        private int colSpan, rowSpan, bandOrder;
        private boolean fontBold;

        public Builder bandId(String v)     { this.bandId = v; return this; }
        public Builder bandText(String v)   { this.bandText = v; return this; }
        public Builder colSpan(int v)       { this.colSpan = v; return this; }
        public Builder rowSpan(int v)       { this.rowSpan = v; return this; }
        public Builder bandOrder(int v)     { this.bandOrder = v; return this; }
        public Builder bgColor(String v)    { this.bgColor = v; return this; }
        public Builder textAlign(String v)  { this.textAlign = v; return this; }
        public Builder fontBold(boolean v)  { this.fontBold = v; return this; }
        public BandMeta build()             { return new BandMeta(this); }
    }

    public String getBandId()           { return bandId; }
    public void setBandId(String v)     { this.bandId = v; }
    public String getBandText()         { return bandText; }
    public void setBandText(String v)   { this.bandText = v; }
    public int getColSpan()             { return colSpan; }
    public void setColSpan(int v)       { this.colSpan = v; }
    public int getRowSpan()             { return rowSpan; }
    public void setRowSpan(int v)       { this.rowSpan = v; }
    public int getBandOrder()           { return bandOrder; }
    public void setBandOrder(int v)     { this.bandOrder = v; }
    public String getBgColor()          { return bgColor; }
    public void setBgColor(String v)    { this.bgColor = v; }
    public String getTextAlign()        { return textAlign; }
    public void setTextAlign(String v)  { this.textAlign = v; }
    public boolean isFontBold()         { return fontBold; }
    public void setFontBold(boolean v)  { this.fontBold = v; }
}
