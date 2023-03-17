package org.openjfx.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.model.OptionalFeatureTypesModel;

public class OptionalFeatureTypesRepository {
    public static List<OptionalFeatureTypesModel> consulta(){
        List<OptionalFeatureTypesModel> listaTabla = new ArrayList<>();
        try{
            Connection connection = conectarDB();

            PreparedStatement preparedStatement=connection.prepareStatement("SELECT * FROM OptionalFeatureTypes");
            ResultSet resultSet=preparedStatement.executeQuery();
            while(resultSet.next()){
                String featureType=resultSet.getString("FeatureType");
                String text=resultSet.getString("Text");
                String featureTypeEsp=resultSet.getString("FeatureTypeEsp");
                String textEsp=resultSet.getString("TextEsp");

                OptionalFeatureTypesModel dato = new OptionalFeatureTypesModel(featureType, text, featureTypeEsp, textEsp);
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
