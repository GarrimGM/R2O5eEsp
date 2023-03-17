package org.openjfx.repositories.model;

public class OptionalFeatureTypesModel {
    public OptionalFeatureTypesModel() {
    }
    public OptionalFeatureTypesModel(String featureType, String text, String featureTypeEsp, String textEsp) {
        this.featureType = featureType;
        this.text = text;
        this.featureTypeEsp = featureTypeEsp;
        this.textEsp = textEsp;
    }
    private String featureType;
    private String text;
    private String featureTypeEsp;
    private String textEsp;
    public String getFeatureType() {
        return featureType;
    }
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getFeatureTypeEsp() {
        return featureTypeEsp;
    }
    public void setFeatureTypeEsp(String featureTypeEsp) {
        this.featureTypeEsp = featureTypeEsp;
    }
    public String getTextEsp() {
        return textEsp;
    }
    public void setTextEsp(String textEsp) {
        this.textEsp = textEsp;
    }
}
