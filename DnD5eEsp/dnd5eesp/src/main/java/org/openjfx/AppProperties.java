package org.openjfx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    
    private static final AppProperties config_file = new AppProperties();
    private Properties prop = new Properties();
    private String msg = "";

    private AppProperties(){
        InputStream input = null;

        try{
            input = App.class.getResourceAsStream("app.properties");

            // Load a properties
            prop.load(input);
        }catch(IOException ex){
            msg = "Can't find/open property file";
            ex.printStackTrace();
        }finally{
            if (input != null){
                try{
                    input.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

        }
    }

    public String getProperty (String key){
        return prop.getProperty(key);
    }

    public String getMsg () {
        return msg;
    }

    // == Singleton design pattern == //
    // Where ever you call this methode in application
    // you always get the same and only instance (config_file)
    public static AppProperties getInstance(){
        return config_file;
    }
}
