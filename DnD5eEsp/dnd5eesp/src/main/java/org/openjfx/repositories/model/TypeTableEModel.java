package org.openjfx.repositories.model;

public class TypeTableEModel {
    public TypeTableEModel() {
    }
    public TypeTableEModel(String source, String text, String classSource, String className, String shortName, String shortNameEsp, String textEsp) {
        this.source = source;
        this.text = text;
        this.classSource = classSource;
        this.className = className;
        this.shortName = shortName;
        this.shortNameEsp = shortNameEsp;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String text;
    private String classSource;
    private String className;
    private String shortName;
    private String shortNameEsp;
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
    public String getClassSource() {
        return classSource;
    }
    public void setClassSource(String classSource) {
        this.classSource = classSource;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getShortNameEsp() {
        return shortNameEsp;
    }
    public void setShortNameEsp(String shortNameEsp) {
        this.shortNameEsp = shortNameEsp;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
