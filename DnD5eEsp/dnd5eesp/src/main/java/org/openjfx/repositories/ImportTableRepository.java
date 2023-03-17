package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.ImportTableModel;

public class ImportTableRepository {
    public static List<ImportTableModel> consulta(){
        List<ImportTableModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();

            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM ImportTable");
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String tableId=resultSet.getString("TableId");
                String jsonDocument=resultSet.getString("JsonDocument");
                String fluffDocument=resultSet.getString("FluffDocument");
                String fieldName=resultSet.getString("FieldName");
                String fluffFieldName=resultSet.getString("FluffFieldName");
                String typeTable=resultSet.getString("TypeTable");
                boolean importDoc=resultSet.getBoolean("ImportDoc");

                ImportTableModel dato = new ImportTableModel(
                    tableId, jsonDocument, fieldName, fluffDocument, fluffFieldName, typeTable, importDoc);
                listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");

        }

        return listaTabla;
    }

    public static List<ImportTableModel> consultaActivos(){
        List<ImportTableModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();

            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM ImportTable WHERE Import=true");
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String tableId=resultSet.getString("TableId");
                String jsonDocument=resultSet.getString("JsonDocument");
                String fluffDocument=resultSet.getString("FluffDocument");
                String fieldName=resultSet.getString("FieldName");
                String fluffFieldName=resultSet.getString("FluffFieldName");
                String typeTable=resultSet.getString("TypeTable");
                boolean importDoc=resultSet.getBoolean("Import");

                ImportTableModel dato = new ImportTableModel(
                    tableId, jsonDocument, fieldName, fluffDocument, fluffFieldName, typeTable, importDoc);
                listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");

        }

        return listaTabla;
    }

    private static Connection conectarDB() {
        Connection connection = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            connection= DriverManager.getConnection("jdbc:ucanaccess://"+AppProperties.getInstance().getProperty("rutaEsp")+"DnD5eEsp.accdb");
        } catch (Exception e) {
            System.out.println("Error al conectar a la base de datos");
        }
        return connection;
    }
}
