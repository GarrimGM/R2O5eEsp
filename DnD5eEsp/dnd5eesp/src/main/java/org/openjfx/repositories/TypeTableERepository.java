package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.TypeTableEModel;

public class TypeTableERepository {
    public static List<TypeTableEModel> consulta(String tabla){
        List<TypeTableEModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM "+tabla);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                 String source=resultSet.getString("Source");
                 String text=resultSet.getString("Text");
                 String classSource=resultSet.getString("ClassSource");
                 String className=resultSet.getString("ClassName");
                 String shortName = resultSet.getString("ShortName");
                 String shortNameEsp = resultSet.getString("ShortNameEsp");
                 String textEsp=resultSet.getString("TextEsp");

                 TypeTableEModel dato = new TypeTableEModel(source, text, classSource, className, shortName, shortNameEsp, textEsp);
                 listaTabla.add(dato);
            }
        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
  }

    public static List<TypeTableEModel> busqueda(String tabla, String codigo, String campo, String texto, String rSource, String rName, String rShortName) {
        List<TypeTableEModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM "+tabla+" WHERE Source=? AND "+campo+"=? AND ClassSource=? AND ClassName=? AND ShortName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, rSource);
            preparedStatement.setString(4, rName);
            preparedStatement.setString(5, rShortName);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String source=resultSet.getString("Source");
                String text=resultSet.getString("Text");
                String classSource=resultSet.getString("ClassSource");
                String className=resultSet.getString("ClassName");
                String shortName = resultSet.getString("ShortName");
                String shortNameEsp = resultSet.getString("ShortNameEsp");
                String textEsp=resultSet.getString("TextEsp");

                TypeTableEModel dato = new TypeTableEModel(source, text, classSource, className, shortName, shortNameEsp, textEsp);
                listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
    }

    public static void alta(String tabla, TypeTableEModel dato) {
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "INSERT INTO "+tabla+" (Source, Text, ClassSource, ClassName, ShortName, ShortNameEsp, TextEsp) VALUES (?,?,?,?,?,?,?)");
            preparedStatement.setString(1, dato.getSource());
            preparedStatement.setString(2, dato.getText());
            preparedStatement.setString(3, dato.getClassSource());
            preparedStatement.setString(4, dato.getClassName());
            preparedStatement.setString(5, dato.getShortName());
            preparedStatement.setString(6, dato.getShortNameEsp());
            preparedStatement.setString(7, dato.getTextEsp());
            preparedStatement.executeUpdate();
            System.out.println("Insert en la tabla "+tabla+", registro: "+dato.getText()+"|"+dato.getSource()+"|"+dato.getClassName()+"|"
                +dato.getClassSource()+"|"+dato.getShortName());
        }catch(Exception e){
            System.out.println("Error al lanzar SQL de alta en la tabla "+tabla);
        }
    }

    public static void borrar(String tabla, String codigo, String texto, String cSource, String cName, String cShortName) {
        Connection connection = conectarDB();
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(
                "DELETE FROM "+tabla+" WHERE Source=? AND Text=? AND ClassSource=? AND ShortName=? AND ClassName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, cSource);
            preparedStatement.setString(4, cShortName);
            preparedStatement.setString(5, cName);
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
