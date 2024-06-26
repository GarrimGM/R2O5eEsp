package org.openjfx.repositories.model;

public class TypeTableFModel {
    public TypeTableFModel() {
    }
    public TypeTableFModel(String source, String text, String classSource, String className, Integer level, String subclassSource, String subclassShortName, String textEsp) {
        this.source = source;
        this.text = text;
        this.classSource = classSource;
        this.className = className;
        this.level = level;
        this.subclassSource = subclassSource;
        this.subclassShortName = subclassShortName;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String text;
    private String classSource;
    private String className;
    private Integer level;
    private String subclassSource;
    private String subclassShortName;
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
    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public String getSubclassSource() {
        return subclassSource;
    }
    public void setSubclassSource(String subclassSource) {
        this.subclassSource = subclassSource;
    }
    public String getSubclassShortName() {
        return subclassShortName;
    }
    public void setSubclassShortName(String subclassShortName) {
        this.subclassShortName = subclassShortName;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
