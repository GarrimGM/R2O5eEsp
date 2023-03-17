package org.openjfx.repositories.model;

public class ImportTableModel {
    public ImportTableModel() {
    }
    public ImportTableModel(String tableId, String jsonDocument, String fieldName, String fluffDocument, 
            String fluffFieldName, String typeTable, boolean importDoc) {
        this.tableId = tableId;
        this.jsonDocument = jsonDocument;
        this.fluffDocument = fluffDocument;
        this.fieldName = fieldName;
        this.typeTable = typeTable;
        this.fluffFieldName = fluffFieldName;
        this.importDoc = importDoc;
    }
    private String tableId;
    private String jsonDocument;
    private String fieldName;
    private String fluffDocument;
    private String fluffFieldName;
    private String typeTable;
    private boolean importDoc;
    public String getTableId() {
        return tableId;
    }
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
    public String getJsonDocument() {
        return jsonDocument;
    }
    public void setJsonDocument(String jsonDocument) {
        this.jsonDocument = jsonDocument;
    }
    public String getFluffDocument() {
        return fluffDocument;
    }
    public void setFluffDocument(String fluffDocument) {
        this.fluffDocument = fluffDocument;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public String getTypeTable() {
        return typeTable;
    }
    public void setTypeTable(String typeTable) {
        this.typeTable = typeTable;
    }
    public boolean isImportDoc() {
        return importDoc;
    }
    public void setImportDoc(boolean importDoc) {
        this.importDoc = importDoc;
    }
    public String getFluffFieldName() {
        return fluffFieldName;
    }
    public void setFluffFieldName(String fluffFieldName) {
        this.fluffFieldName = fluffFieldName;
    }
}
