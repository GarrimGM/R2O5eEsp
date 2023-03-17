package org.openjfx.repositories.model;

public class SourcesModel {
    public SourcesModel() {
    }
    public SourcesModel(String type, String orden, String source, String sourceEsp, String text, String textEsp,
            String color, boolean comparate) {
        this.type = type;
        this.orden = orden;
        this.source = source;
        this.sourceEsp = sourceEsp;
        this.text = text;
        this.textEsp = textEsp;
        this.color = color;
        this.comparate = comparate;
    }
    private String type;
    private String orden;
    private String source;
    private String sourceEsp;
    private String text;
    private String textEsp;
    private String color;
    private boolean comparate;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getOrden() {
        return orden;
    }
    public void setOrden(String orden) {
        this.orden = orden;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSourceEsp() {
        return sourceEsp;
    }
    public void setSourceEsp(String sourceEsp) {
        this.sourceEsp = sourceEsp;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public boolean getComparate() {
        return comparate;
    }
    public void setComparate(boolean comparate) {
        this.comparate = comparate;
    }
}
