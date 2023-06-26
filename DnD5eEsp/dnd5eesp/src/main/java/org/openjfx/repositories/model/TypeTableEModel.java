package org.openjfx.repositories.model;

public class TypeTableEModel {
    public TypeTableEModel() {
    }
    public TypeTableEModel(String source, String text, String classSource, String className, String subclassShortName, String subclassShortNameEsp, String textEsp) {
        this.source = source;
        this.text = text;
        this.classSource = classSource;
        this.className = className;
        this.subclassShortName = subclassShortName;
        this.subclassShortNameEsp = subclassShortNameEsp;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String text;
    private String classSource;
    private String className;
    private String subclassShortName;
    private String subclassShortNameEsp;
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
    public String getSubclassShortName() {
        return subclassShortName;
    }
    public void setSubclassShortName(String subclassShortName) {
        this.subclassShortName = subclassShortName;
    }
    public String getSubclassShortNameEsp() {
        return subclassShortNameEsp;
    }
    public void setSubclassShortNameEsp(String subclassShortNameEsp) {
        this.subclassShortNameEsp = subclassShortNameEsp;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
