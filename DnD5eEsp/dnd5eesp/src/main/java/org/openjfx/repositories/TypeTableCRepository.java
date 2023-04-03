package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.TypeTableCModel;

public class TypeTableCRepository {
    public static List<TypeTableCModel> consulta(String tabla){
        List<TypeTableCModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM "+tabla);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                 String source=resultSet.getString("Source");
                 String text=resultSet.getString("Text");
                 String raceSource=resultSet.getString("RaceSource");
                 String raceName=resultSet.getString("RaceName");
                 String textEsp=resultSet.getString("TextEsp");

                 TypeTableCModel dato = new TypeTableCModel(source, text, raceSource, raceName, textEsp);
                 listaTabla.add(dato);
            }
        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
  }

    public static List<TypeTableCModel> busqueda(String tabla, String codigo, String campo, String texto, String rSource, String rName) {
        List<TypeTableCModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "SELECT * FROM "+tabla+" WHERE Source=? AND "+campo+"=? AND RaceSource=? AND RaceName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, rSource);
            preparedStatement.setString(4, rName);
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String source=resultSet.getString("Source");
                String text=resultSet.getString("Text");
                String raceSource=resultSet.getString("RaceSource");
                String raceName=resultSet.getString("RaceName");
                String textEsp=resultSet.getString("TextEsp");

                TypeTableCModel dato = new TypeTableCModel(source, text, raceSource, raceName, textEsp);
                listaTabla.add(dato);
            }

        }catch(Exception e){
            System.out.println("Error al lanzar la consulta SQL");
        }
        return listaTabla;
    }

    public static void alta(String tabla, TypeTableCModel dato) {
        try{
            Connection connection = conectarDB();
            PreparedStatement preparedStatement=connection.prepareStatement(
                "INSERT INTO "+tabla+" (Source, Text, RaceSource, RaceName, TextEsp) VALUES (?,?,?,?,?)");
            preparedStatement.setString(1, dato.getSource());
            preparedStatement.setString(2, dato.getText());
            preparedStatement.setString(3, dato.getRaceSource());
            preparedStatement.setString(4, dato.getRaceName());
            preparedStatement.setString(5, dato.getTextEsp());
            preparedStatement.executeUpdate();
            System.out.println("Insert en la tabla "+tabla+", registro: "+dato.getText()+"|"+dato.getSource()+"|"+dato.getRaceName()+"|"+dato.getRaceSource());
        }catch(Exception e){
            System.out.println("Error al lanzar SQL de alta en la tabla "+tabla);
        }
    }

    public static void borrar(String tabla, String codigo, String texto, String rSource, String rName) {
        Connection connection = conectarDB();
        try{
            PreparedStatement preparedStatement=connection.prepareStatement(
                "DELETE FROM "+tabla+" WHERE Source=? AND Text=? AND RaceSource=? AND RaceName=?");
            preparedStatement.setString(1, codigo);
            preparedStatement.setString(2, texto);
            preparedStatement.setString(3, rSource);
            preparedStatement.setString(4, rName);
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
