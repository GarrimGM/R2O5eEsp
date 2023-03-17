package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.SourcesModel;

public class SourcesRepository {
    public static List<SourcesModel> consulta(){
        List<SourcesModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();

            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM Sources ORDER BY Type, Orden ASC");
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String type=resultSet.getString("Type");
                String orden=resultSet.getString("Orden");
                String source=resultSet.getString("Source");
                String sourceEsp=resultSet.getString("SourceEsp");
                String text=resultSet.getString("Text");
                String textEsp=resultSet.getString("TextEsp");
                String color=resultSet.getString("Color");
                boolean comparate=resultSet.getBoolean("Comparate");

                SourcesModel dato = new SourcesModel(type, orden, source, sourceEsp, text, textEsp, color, comparate);
                listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");

        }

        return listaTabla;
    }

    public static List<SourcesModel> consultaActivos(){
        List<SourcesModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();

            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM Sources WHERE Comparate=true ORDER BY Type, Orden ASC");
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String type=resultSet.getString("Type");
                String orden=resultSet.getString("Orden");
                String source=resultSet.getString("Source");
                String sourceEsp=resultSet.getString("SourceEsp");
                String text=resultSet.getString("Text");
                String textEsp=resultSet.getString("TextEsp");
                String color=resultSet.getString("Color");
                boolean comparate=resultSet.getBoolean("Comparate");

                SourcesModel dato = new SourcesModel(type, orden, source, sourceEsp, text, textEsp, color, comparate);
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
