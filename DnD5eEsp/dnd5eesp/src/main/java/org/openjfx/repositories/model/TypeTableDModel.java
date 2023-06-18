package org.openjfx.repositories.model;

public class TypeTableDModel {
    public TypeTableDModel() {
    }
    public TypeTableDModel(String source, String text, String classSource, String className, String textEsp) {
        this.source = source;
        this.text = text;
        this.classSource = classSource;
        this.className = className;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String text;
    private String classSource;
    private String className;
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
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
