package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.TypeTableGModel;

public class TypeTableGRepository {
    public static List<TypeTableGModel> consulta(String tabla){
        List<TypeTableGModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM "+tabla);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                 String source=resultSet.getString("Source");
                 String pantheon=resultSet.getString("Pantheon");
                 String text=resultSet.getString("Text");
                 String pantheonEsp=resultSet.getString("PantheonEsp");
                 String textEsp=resultSet.getString("TextEsp");

                 TypeTableGModel dato = new TypeTableGModel(source, pantheon, text, pantheonEsp, textEsp);
                 listaTabla.add(dato);
            }
        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
  }

    public static List<TypeTableGModel> busqueda(String tabla, String codigo, String campo, String texto, String pantheon) {
        List<TypeTableGModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM "+tabla+" WHERE Source=? AND "+campo+"=? AND Pantheon=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, pantheon);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                    String source=resultSet.getString("Source");
                    String text=resultSet.getString("Text");
                    String textEsp=resultSet.getString("TextEsp");
                    String pantheonEsp=resultSet.getString("PantheonEsp");

                    TypeTableGModel dato = new TypeTableGModel(source, pantheon, text, pantheonEsp, textEsp);
                    listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
    }

    public static void alta(String tabla, TypeTableGModel dato) {
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "INSERT INTO "+tabla+" (Source, Pantheon, Text, PantheonEsp, TextEsp) VALUES (?,?,?,?,?)");
            preparedStatement.setString(1, dato.getSource());
            preparedStatement.setString(2, dato.getPantheon());
            preparedStatement.setString(3, dato.getText());
            preparedStatement.setString(4, dato.getPantheonEsp());
            preparedStatement.setString(5, dato.getTextEsp());
            preparedStatement.executeUpdate();
            System.out.println("Insert en la tabla "+tabla+", registro: "+dato.getText()+"|"+dato.getPantheon()+"|"+dato.getSource());
        }catch(Exception e){
            System.out.println("Error al lanzar SQL de alta en la tabla "+tabla);
        }
    }

    public static void borrar(String tabla, String codigo, String texto, String pantheon) {
        Connection connection = conectarDB();
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(
                "DELETE FROM "+tabla+" WHERE Source=? AND Text=? AND Pantheon=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, pantheon);
            preparedStatement.executeUpdate();
        }catch(Exception e){
            System.out.println("Error al lanzar SQL de borrar");
        }
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
