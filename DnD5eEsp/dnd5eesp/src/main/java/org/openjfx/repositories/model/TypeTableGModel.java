package org.openjfx.repositories.model;

public class TypeTableGModel {
    public TypeTableGModel() {
    }
    public TypeTableGModel(String source, String text, String textEsp) {
        this.source = source;
        this.text = text;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String pantheon;
    private String text;
    private String pantheonEsp;
    private String textEsp;

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
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

}
