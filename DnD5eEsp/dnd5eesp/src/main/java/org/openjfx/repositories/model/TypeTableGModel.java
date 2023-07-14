package org.openjfx.repositories.model;

public class TypeTableGModel {
    public TypeTableGModel() {
    }
    public TypeTableGModel(String source, String pantheon, String text, String pantheonEsp, String textEsp) {
        this.source = source;
        this.pantheon = pantheon;
        this.text = text;
        this.pantheonEsp = pantheonEsp;
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
    public String getPantheon() {
        return pantheon;
    }
    public void setPantheon(String pantheon) {
        this.pantheon = pantheon;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getPantheonEsp() {
        return pantheonEsp;
    }
    public void setPantheonEsp(String pantheonEsp) {
        this.pantheonEsp = pantheonEsp;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
