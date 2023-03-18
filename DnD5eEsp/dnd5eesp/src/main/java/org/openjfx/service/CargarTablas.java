package org.openjfx.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.ImportTableRepository;
import org.openjfx.repositories.SourcesRepository;
import org.openjfx.repositories.TypeTableARepository;
import org.openjfx.repositories.model.ImportTableModel;
import org.openjfx.repositories.model.SourcesModel;
import org.openjfx.repositories.model.TypeTableAModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CargarTablas {
    public static void cargarTablas() throws IOException {
        System.out.println("Inicio - Carga de tablas");
        //Consulta la lista de sources activos para comparar
        //creo un hashmap para solo tener que recorrer esta lista una vez
        HashMap<String,SourcesModel> sourcesMap = new HashMap<>();
        List<SourcesModel> listaSources = SourcesRepository.consultaActivos();
        for (SourcesModel source : listaSources) {
            sourcesMap.put(source.getSource(), source);
        }

        //Creamos un hashmap para controlar que registror ha encontrado
        //para luego poder borar registros que ya no existen
        HashMap<String,String> foundMap = new HashMap<>();

        //Consulta la lista de documentos a importar
        List<ImportTableModel> listaTablas = ImportTableRepository.consultaActivos();
        for (ImportTableModel importTable : listaTablas) {
            System.out.println("Inicio - Proceso de carga de la tabla "+importTable.getTableId());
            //Recupera el json del documento
            String rutaDocOriginal = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+importTable.getJsonDocument();
            String rutaDocEsp = AppProperties.getInstance().getProperty("rutaEsp")+"data\\"+importTable.getJsonDocument();
            //Comprobar el tipo de tabla
            if(importTable.getTypeTable().equals("A")) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                    JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                    //Recorre la lista original y mira si es uno de los sources a traducir
                    for (int i = 0; i < listaDatos.size(); i++) {
                        JsonObject dato = listaDatos.get(i).getAsJsonObject();
                        String source = dato.get("source").getAsString();
                        String text = dato.get("name").getAsString();
                        if(sourcesMap.containsKey(source)) {
                            String sourceEsp = sourcesMap.get(source).getSourceEsp();
                            String textEsp = "";
                            //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                            String keyDocument = text+"|"+source;
                            foundMap.put(keyDocument, keyDocument);
                            //Busca en la tabla si ya esta esta el registro del documento
                            List<TypeTableAModel> busqueda = TypeTableARepository.busqueda(importTable.getTableId(), source, "Text", text);
                            if(busqueda.size()>0) {
                                //Carga la variable de traducción
                                textEsp = busqueda.get(0).getTextEsp();
                            } else {
                                //Como aún no existe se añade a la tabla de destino
                                TypeTableARepository.alta(importTable.getTableId(), new TypeTableAModel(source, text, ""));
                            }
                            buscarActDocA(importTable, rutaDocEsp, dato, source, sourceEsp, text, textEsp);
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Documento original no encontrado");
                }
            } else if(importTable.getTypeTable().equals("B")) {
                //Tabla de magicitems
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                    JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                    //Recorre la lista original y mira si es uno de los sources a traducir
                    for (int i = 0; i < listaDatos.size(); i++) {
                        JsonObject dato = listaDatos.get(i).getAsJsonObject();
                        JsonObject datoHeredado = dato.getAsJsonObject("inherits");
                        String source = datoHeredado.get("source").getAsString();
                        String text = dato.get("name").getAsString();
                        if(sourcesMap.containsKey(source)) {
                            String sourceEsp = sourcesMap.get(source).getSourceEsp();
                            String textEsp = "";
                            //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                            String keyDocument = text+"|"+source;
                            foundMap.put(keyDocument, keyDocument);
                            //Busca en la tabla si ya esta esta el registro del documento
                            List<TypeTableAModel> busqueda = TypeTableARepository.busqueda(importTable.getTableId(), source, "Text", text);
                            if(busqueda.size()>0) {
                                //Carga la variable de traducción
                                textEsp = busqueda.get(0).getTextEsp();
                            } else {
                                //Como aún no existe se añade a la tabla de destino
                                TypeTableARepository.alta(importTable.getTableId(), new TypeTableAModel(source, text, ""));
                            }
                            buscarActDocB(importTable, rutaDocEsp, dato, source, sourceEsp, text, textEsp);
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Documento original no encontrado");
                }
            }
            System.out.println("Fin - Proceso de carga de la tabla "+importTable.getTableId());
        }

        for (ImportTableModel importTable : listaTablas) {
            List<TypeTableAModel> consultaTabla = TypeTableARepository.consulta(importTable.getTableId());
            for (TypeTableAModel registro : consultaTabla) {
                if(!foundMap.containsKey(registro.getText()+"|"+registro.getSource())) {
                    TypeTableARepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText());
                    System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                }
            }
        }

        System.out.println("Fin - Carga de tablas");
    }

    private static void buscarActDocA(ImportTableModel importTable,
            String rutaDocEsp, JsonObject dato, String source, String sourceEsp, String text, String textEsp) throws IOException {
        //Comprueba si existe el documento json de destino
        JsonObject jsonEsp = new JsonObject();
        try {
            BufferedReader bufferedReaderEsp = new BufferedReader(new FileReader(rutaDocEsp));
            Gson gsonEsp = new Gson();
            jsonEsp = gsonEsp.fromJson(bufferedReaderEsp, JsonObject.class);
            //Comprueba si tiene el campo
            if(!jsonEsp.has(importTable.getFieldName())){
                JsonArray jsLista = new JsonArray();
                jsonEsp.add(importTable.getFieldName(), jsLista);
            }
        } catch (FileNotFoundException e) {
            //Si no encuentra el documento lo crea
            JsonArray jsLista = new JsonArray();
            jsonEsp.add(importTable.getFieldName(), jsLista);
            try (Writer writer = new FileWriter(rutaDocEsp)) {
                Gson gsonCreate = new GsonBuilder().create();
                gsonCreate.toJson(jsonEsp, writer);
            }
            System.out.println("Documento "+importTable.getJsonDocument()+" creado");
        }
        JsonArray listaDatosEsp = jsonEsp.get(importTable.getFieldName()).getAsJsonArray();
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            String sourceDocEsp = datoEsp.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            if(sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            //Comprobamos si tienes token
            if(dato.has("hasToken")) {
                dato.remove("hasToken");
                String rutaToken = "img/";
                if(importTable.getFieldName().equals("object")) {
                    rutaToken = rutaToken+"vehicles/tokens/";
                }
                rutaToken = rutaToken+source+"/";
                String nombreToken = text.trim().replaceAll(" ", "%20")+".png";
                String urlToken = AppProperties.getInstance().getProperty("urlToken")+rutaToken+nombreToken;
                dato.addProperty("tokenUrl", urlToken);
            }
            //Comprobamos si esta tabla puede tener fluff
            if(importTable.getFluffDocument()!=null && !importTable.getFluffDocument().isEmpty()) {
                if(dato.has("hasFluff") || dato.has("hasFluffImages")){
                    dato.remove("hasFluff");
                    dato.remove("hasFluffImages");
                    //Recuperamos el documento fluff
                    String rutaDocFluff = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+importTable.getFluffDocument();
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocFluff));
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                    JsonArray listaDatosFluff = json.get(importTable.getFluffFieldName()).getAsJsonArray();
                    //Buscamos el identificador en la lista de fluff
                    for (int i = 0; i < listaDatosFluff.size(); i++) {
                        JsonObject datoFluff = listaDatosFluff.get(i).getAsJsonObject();
                        String sourceDocFluff = datoFluff.get("source").getAsString();
                        String textDocFluff = datoFluff.get("name").getAsString();
                        if(sourceDocFluff.equals(source) &&
                                textDocFluff.equals(text)) {
                            datoFluff.remove("name");
                            datoFluff.remove("source");
                            //Se añade al fluff al dato que se llevará al json
                            dato.add("fluff", datoFluff);
                            i = listaDatosFluff.size();
                        }
                    }    
                }
            }

            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            try (Writer writer = new FileWriter(rutaDocEsp)) {
                Gson gsonCreate = new GsonBuilder().create();
                gsonCreate.toJson(jsonEsp, writer);
            }
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                +text+"|"+source);
        }
    }

    private static void buscarActDocB(ImportTableModel importTable,
            String rutaDocEsp, JsonObject dato, String source, String sourceEsp, String text, String textEsp) throws IOException {
        //Comprueba si existe el documento json de destino
        JsonObject jsonEsp = new JsonObject();
        try {
            BufferedReader bufferedReaderEsp = new BufferedReader(new FileReader(rutaDocEsp));
            Gson gsonEsp = new Gson();
            jsonEsp = gsonEsp.fromJson(bufferedReaderEsp, JsonObject.class);
            //Comprueba si tiene el campo
            if(!jsonEsp.has(importTable.getFieldName())){
                JsonArray jsLista = new JsonArray();
                jsonEsp.add(importTable.getFieldName(), jsLista);
            }
        } catch (FileNotFoundException e) {
            //Si no encuentra el documento lo crea
            JsonArray jsLista = new JsonArray();
            jsonEsp.add(importTable.getFieldName(), jsLista);
            try (Writer writer = new FileWriter(rutaDocEsp)) {
                Gson gsonCreate = new GsonBuilder().create();
                gsonCreate.toJson(jsonEsp, writer);
            }
            System.out.println("Documento "+importTable.getJsonDocument()+" creado");
        }
        JsonArray listaDatosEsp = jsonEsp.get(importTable.getFieldName()).getAsJsonArray();
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            JsonObject datoEspHeredado = datoEsp.getAsJsonObject("inherits");
            String sourceDocEsp = datoEspHeredado.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            if(sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            JsonObject datoHeredado = dato.getAsJsonObject("inherits");
            datoHeredado.addProperty("source", sourceEsp);
            dato.add("inherits", datoHeredado);
            //Comprobamos si tienes token
            if(dato.has("hasToken")) {
                dato.remove("hasToken");
                String rutaToken = "img/";
                if(importTable.getFieldName().equals("object")) {
                    rutaToken = rutaToken+"vehicles/tokens/";
                }
                rutaToken = rutaToken+source+"/";
                String nombreToken = text.trim().replaceAll(" ", "%20")+".png";
                String urlToken = AppProperties.getInstance().getProperty("urlToken")+rutaToken+nombreToken;
                dato.addProperty("tokenUrl", urlToken);
            }
            //Comprobamos si esta tabla puede tener fluff
            if(importTable.getFluffDocument()!=null && !importTable.getFluffDocument().isEmpty()) {
                if(dato.has("hasFluff") || dato.has("hasFluffImages")){
                    dato.remove("hasFluff");
                    dato.remove("hasFluffImages");
                    //Recuperamos el documento fluff
                    String rutaDocFluff = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+importTable.getFluffDocument();
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocFluff));
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                    JsonArray listaDatosFluff = json.get(importTable.getFluffFieldName()).getAsJsonArray();
                    //Buscamos el identificador en la lista de fluff
                    for (int i = 0; i < listaDatosFluff.size(); i++) {
                        JsonObject datoFluff = listaDatosFluff.get(i).getAsJsonObject();
                        String sourceDocFluff = datoFluff.get("source").getAsString();
                        String textDocFluff = datoFluff.get("name").getAsString();
                        if(sourceDocFluff.equals(source) &&
                                textDocFluff.equals(text)) {
                            datoFluff.remove("name");
                            datoFluff.remove("source");
                            //Se añade al fluff al dato que se llevará al json
                            dato.add("fluff", datoFluff);
                            i = listaDatosFluff.size();
                        }
                    }    
                }
            }

            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            try (Writer writer = new FileWriter(rutaDocEsp)) {
                Gson gsonCreate = new GsonBuilder().create();
                gsonCreate.toJson(jsonEsp, writer);
            }
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                +text+"|"+source);
        }
    }
}
