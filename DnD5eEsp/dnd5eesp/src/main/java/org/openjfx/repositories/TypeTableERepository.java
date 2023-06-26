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
                 String subclassShortName = resultSet.getString("SubclassShortName");
                 String subclassShortNameEsp = resultSet.getString("SubclassShortNameEsp");
                 String textEsp=resultSet.getString("TextEsp");

                 TypeTableEModel dato = new TypeTableEModel(source, text, classSource, className, subclassShortName, subclassShortNameEsp, textEsp);
                 listaTabla.add(dato);
            }
        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
  }

    public static List<TypeTableEModel> busqueda(String tabla, String codigo, String campo, String texto, String rSource, String rName, String rSubclassShortName) {
        List<TypeTableEModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM "+tabla+" WHERE Source=? AND "+campo+"=? AND ClassSource=? AND ClassName=? AND SubclassShortName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, rSource);
            preparedStatement.setString(4, rName);
            preparedStatement.setString(5, rSubclassShortName);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String source=resultSet.getString("Source");
                String text=resultSet.getString("Text");
                String classSource=resultSet.getString("ClassSource");
                String className=resultSet.getString("ClassName");
                String subclassShortName = resultSet.getString("SubclassShortName");
                String subclassShortNameEsp = resultSet.getString("SubclassShortNameEsp");
                String textEsp=resultSet.getString("TextEsp");

                TypeTableEModel dato = new TypeTableEModel(source, text, classSource, className, subclassShortName, subclassShortNameEsp, textEsp);
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
                "INSERT INTO "+tabla+" (Source, Text, ClassSource, ClassName, SubclassShortName, SubclassShortNameEsp, TextEsp) VALUES (?,?,?,?,?,?,?)");
            preparedStatement.setString(1, dato.getSource());
            preparedStatement.setString(2, dato.getText());
            preparedStatement.setString(3, dato.getClassSource());
            preparedStatement.setString(4, dato.getClassName());
            preparedStatement.setString(5, dato.getSubclassShortName());
            preparedStatement.setString(6, dato.getSubclassShortNameEsp());
            preparedStatement.setString(7, dato.getTextEsp());
            preparedStatement.executeUpdate();
            System.out.println("Insert en la tabla "+tabla+", registro: "+dato.getText()+"|"+dato.getSource()+"|"+dato.getClassName()+"|"+dato.getClassSource()+"|"+dato.getSubclassShortName());
        }catch(Exception e){
            System.out.println("Error al lanzar SQL de alta en la tabla "+tabla);
        }
    }

    public static void borrar(String tabla, String codigo, String texto, String cSource, String cName, String cSubclassShortName) {
        Connection connection = conectarDB();
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(
                "DELETE FROM "+tabla+" WHERE Source=? AND Text=? AND ClassSource=? AND SubclassShortName=? AND ClassName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, cSource);
            preparedStatement.setString(4, cSubclassShortName);
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
