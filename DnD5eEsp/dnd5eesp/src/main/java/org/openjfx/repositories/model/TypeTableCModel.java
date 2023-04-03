package org.openjfx.repositories.model;

public class TypeTableCModel {
    public TypeTableCModel() {
    }
    public TypeTableCModel(String source, String text, String raceSource, String raceName, String textEsp) {
        this.source = source;
        this.text = text;
        this.raceSource = raceSource;
        this.raceName = raceName;
        this.textEsp = textEsp;
    }
    
    private String source;
    private String text;
    private String raceSource;
    private String raceName;
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
    public String getRaceSource() {
        return raceSource;
    }
    public void setRaceSource(String raceSource) {
        this.raceSource = raceSource;
    }
    public String getRaceName() {
        return raceName;
    }
    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }

}
