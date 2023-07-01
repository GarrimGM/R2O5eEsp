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
import org.openjfx.repositories.TypeTableCRepository;
import org.openjfx.repositories.TypeTableDRepository;
import org.openjfx.repositories.TypeTableERepository;
import org.openjfx.repositories.TypeTableFRepository;
import org.openjfx.repositories.model.ImportTableModel;
import org.openjfx.repositories.model.SourcesModel;
import org.openjfx.repositories.model.TypeTableAModel;
import org.openjfx.repositories.model.TypeTableCModel;
import org.openjfx.repositories.model.TypeTableDModel;
import org.openjfx.repositories.model.TypeTableEModel;
import org.openjfx.repositories.model.TypeTableFModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class CargarTablas extends Service<Void>{

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                cargarTablas();
                return null;
            }
        };
    }

    private static void cargarTablas() throws IOException {
        System.out.println("--- Inicio - Carga de tablas");
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
            if(!importTable.getTypeTable().equals("X")) {
                System.out.println("Inicio - Proceso de carga de la tabla "+importTable.getTableId());
                //Recupera el json del documento
                String rutaDocOriginal = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+importTable.getJsonDocument();
                String rutaDocEsp = AppProperties.getInstance().getProperty("rutaEsp")+"data\\"+importTable.getJsonDocument();

                //Comprueba si existe el documento json de destino
                JsonObject jsonEsp = new JsonObject();
                try {
                    BufferedReader bufferedReaderEsp = new BufferedReader(new FileReader(rutaDocEsp));
                    Gson gsonEsp = new GsonBuilder().serializeNulls().create();
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
                        Gson gsonCreate = new GsonBuilder().serializeNulls().create();
                        gsonCreate.toJson(jsonEsp, writer);
                    }
                    System.out.println("Documento "+importTable.getJsonDocument()+" creado");
                }
                JsonObject jsonEspOriginal = jsonEsp.deepCopy();
                JsonArray listaDatosEsp = jsonEsp.get(importTable.getFieldName()).getAsJsonArray();

                //Recuperamos el documento fluff
                JsonArray listaDatosFluff = new JsonArray();
                HashMap<String,JsonObject> fluffMap = new HashMap<>();
                if(importTable.getFluffDocument()!=null && !importTable.getFluffDocument().isEmpty()) {
                    try {
                        String rutaDocFluff = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+importTable.getFluffDocument();
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocFluff));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        listaDatosFluff = json.get(importTable.getFluffFieldName()).getAsJsonArray();

                        for (int i = 0; i < listaDatosFluff.size(); i++) {
                            JsonObject datoFluff = listaDatosFluff.get(i).getAsJsonObject();
                            String sourceDocFluff = datoFluff.get("source").getAsString();
                            String textDocFluff = datoFluff.get("name").getAsString();
                            fluffMap.put(importTable.getTableId()+"|"+textDocFluff+"|"+sourceDocFluff, datoFluff);
                        }

                        if(importTable.getTableId().equals("Race") || importTable.getTableId().equals("Subrace")) {
                            JsonObject raceFluffMetaUncommon = json.get("raceFluffMeta").getAsJsonObject().get("uncommon").getAsJsonObject();
                            fluffMap.put("raceFluffMeta|uncommon", raceFluffMetaUncommon);
                            JsonObject raceFluffMetaMonstrous = json.get("raceFluffMeta").getAsJsonObject().get("monstrous").getAsJsonObject();
                            fluffMap.put("raceFluffMeta|monstrous", raceFluffMetaMonstrous);
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento Fluff "+importTable.getFluffDocument()+" no encontrado");
                    }
                }

                //Comprobar el tipo de tabla
                if(importTable.getTypeTable().equals("A")) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            String source = dato.get("source").getAsString();
                            if(sourcesMap.containsKey(source) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source;
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
                                jsonEsp = buscarActDocA(jsonEsp, importTable, listaDatosEsp, fluffMap, dato, source, sourceEsp, text, textEsp);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                } else if(importTable.getTypeTable().equals("B")) {
                    //Tabla de magicitems
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            JsonObject datoHeredado = dato.getAsJsonObject("inherits");
                            String source = datoHeredado.get("source").getAsString();
                            if(sourcesMap.containsKey(source) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source;
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
                                jsonEsp = buscarActDocB(jsonEsp, importTable, listaDatosEsp, fluffMap, dato, source, sourceEsp, text, textEsp);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                } else if(importTable.getTypeTable().equals("C")) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            String source = dato.get("source").getAsString();
                            String raceSource = dato.get("raceSource").getAsString();
                            String raceName = dato.get("raceName").getAsString();
                            if(sourcesMap.containsKey(source) && sourcesMap.containsKey(raceSource) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                String raceSourceEsp = sourcesMap.get(raceSource).getSourceEsp();
                                String raceNameEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source+"|"+raceSource+"|"+raceName;
                                foundMap.put(keyDocument, keyDocument);
                                //Busca en la tabla si ya esta esta el registro del documento
                                List<TypeTableCModel> busqueda = TypeTableCRepository.busqueda(importTable.getTableId(), source, "Text", text, raceSource, raceName);
                                if(busqueda.size()>0) {
                                    //Carga la variable de traducción
                                    textEsp = busqueda.get(0).getTextEsp();
                                } else {
                                    //Como aún no existe se añade a la tabla de destino
                                    TypeTableCRepository.alta(importTable.getTableId(), new TypeTableCModel(source, text, raceSource, raceName, ""));
                                }
                                //Buscamos la traducción de la raza a la que pertenece
                                List<TypeTableAModel> busquedaRace = TypeTableARepository.busqueda("Race", raceSource, "Text", raceName);
                                if(busquedaRace.size()>0) {
                                    //Carga la variable de traducción
                                    raceNameEsp = busquedaRace.get(0).getTextEsp();
                                }
                                jsonEsp = buscarActDocC(jsonEsp, importTable, listaDatosEsp, fluffMap, dato, source, sourceEsp, text, textEsp, raceSource, raceSourceEsp, raceName , raceNameEsp);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                } else if(importTable.getTypeTable().equals("D")) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            String source = dato.get("source").getAsString();
                            String classSource = dato.get("classSource").getAsString();
                            String className = dato.get("className").getAsString();
                            Integer level = dato.get("level").getAsInt();
                            if(sourcesMap.containsKey(source) && sourcesMap.containsKey(classSource) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                String classSourceEsp = sourcesMap.get(classSource).getSourceEsp();
                                String classNameEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source+"|"+classSource+"|"+className+"|"+level;
                                foundMap.put(keyDocument, keyDocument);
                                //Busca en la tabla si ya esta esta el registro del documento
                                List<TypeTableDModel> busqueda = TypeTableDRepository.busqueda(importTable.getTableId(), source, "Text", text, classSource, className, level);
                                if(busqueda.size()>0) {
                                    //Carga la variable de traducción
                                    textEsp = busqueda.get(0).getTextEsp();
                                } else {
                                    //Como aún no existe se añade a la tabla de destino
                                    TypeTableDRepository.alta(importTable.getTableId(), new TypeTableDModel(source, text, classSource, className, level, ""));
                                }
                                //Buscamos la traducción de la raza a la que pertenece
                                List<TypeTableAModel> busquedaRace = TypeTableARepository.busqueda("Class", classSource, "Text", className);
                                if(busquedaRace.size()>0) {
                                    //Carga la variable de traducción
                                    classNameEsp = busquedaRace.get(0).getTextEsp();
                                }
                                jsonEsp = buscarActDocD(jsonEsp, importTable, listaDatosEsp, dato, source, sourceEsp, text, textEsp, classSource, classSourceEsp, className , classNameEsp, level);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                } else if(importTable.getTypeTable().equals("E")) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            String source = dato.get("source").getAsString();
                            String classSource = dato.get("classSource").getAsString();
                            String className = dato.get("className").getAsString();
                            String shortName = dato.get("shortName").getAsString();
                            if(sourcesMap.containsKey(source) && sourcesMap.containsKey(classSource) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                String classSourceEsp = sourcesMap.get(classSource).getSourceEsp();
                                String classNameEsp = "";
                                String shortNameEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source+"|"+classSource+"|"+className+"|"+shortName;
                                foundMap.put(keyDocument, keyDocument);
                                //Busca en la tabla si ya esta esta el registro del documento
                                List<TypeTableEModel> busqueda = TypeTableERepository.busqueda(importTable.getTableId(), source, "Text", text, 
                                    classSource, className, shortName);
                                if(busqueda.size()>0) {
                                    //Carga la variable de traducción
                                    textEsp = busqueda.get(0).getTextEsp();
                                } else {
                                    //Como aún no existe se añade a la tabla de destino
                                    TypeTableERepository.alta(importTable.getTableId(), 
                                        new TypeTableEModel(source, text, classSource, className, shortName, "", ""));
                                }
                                //Buscamos la traducción de la clase a la que pertenece
                                List<TypeTableAModel> busquedaClass = TypeTableARepository.busquedaClassNameEsp(className);
                                if(busquedaClass.size()>0) {
                                    //Carga la variable de traducción
                                    classNameEsp = busquedaClass.get(0).getTextEsp();
                                }
                                //Buscamos la traducción de la subclase abreviada
                                List<TypeTableEModel> busquedaSubclass = TypeTableERepository.busquedaSubclassShortnameEsp(className, shortName);
                                if(busquedaSubclass.size()>0) {
                                    //Carga la variable de traducción
                                    shortNameEsp = busquedaSubclass.get(0).getShortNameEsp();
                                }
                                jsonEsp = buscarActDocE(jsonEsp, importTable, listaDatosEsp, dato, source, sourceEsp, text, textEsp, 
                                    classSource, classSourceEsp, className , classNameEsp, shortName, shortNameEsp);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                } else if(importTable.getTypeTable().equals("F")) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaDocOriginal));
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);
                        JsonArray listaDatos = json.get(importTable.getFieldName()).getAsJsonArray();
                        //Recorre la lista original y mira si es uno de los sources a traducir
                        for (int i = 0; i < listaDatos.size(); i++) {
                            JsonObject dato = listaDatos.get(i).getAsJsonObject();
                            String source = dato.get("source").getAsString();
                            String classSource = dato.get("classSource").getAsString();
                            String className = dato.get("className").getAsString();
                            Integer level = dato.get("level").getAsInt();
                            String subclassSource = dato.get("subclassSource").getAsString();
                            String subclassShortName = dato.get("subclassShortName").getAsString();
                            if(sourcesMap.containsKey(source) && sourcesMap.containsKey(classSource) && sourcesMap.containsKey(subclassSource) && dato.has("name")) {
                                String text = dato.get("name").getAsString();
                                String sourceEsp = sourcesMap.get(source).getSourceEsp();
                                String textEsp = "";
                                String classSourceEsp = sourcesMap.get(classSource).getSourceEsp();
                                String classNameEsp = "";
                                String subclassSourceEsp = sourcesMap.get(subclassSource).getSourceEsp();
                                String subclassShortNameEsp = "";
                                //Guardamos en el map la clave para ver si sobran registros luego en la base de datos
                                String keyDocument = importTable.getTableId()+"|"+text+"|"+source+"|"+classSource+"|"+className+"|"+level+"|"+subclassShortName;
                                foundMap.put(keyDocument, keyDocument);
                                //Busca en la tabla si ya esta esta el registro del documento
                                List<TypeTableFModel> busqueda = TypeTableFRepository.busqueda(importTable.getTableId(), source, "Text", text, 
                                    classSource, className, level, subclassSource, subclassShortName);
                                if(busqueda.size()>0) {
                                    //Carga la variable de traducción
                                    textEsp = busqueda.get(0).getTextEsp();
                                } else {
                                    //Como aún no existe se añade a la tabla de destino
                                    TypeTableFRepository.alta(importTable.getTableId(), new TypeTableFModel(source, text, classSource, 
                                        className, level, subclassSource, subclassShortName, ""));
                                }
                                //Buscamos la traducción de la clase a la que pertenece
                                List<TypeTableAModel> busquedaClass = TypeTableARepository.busquedaClassNameEsp(className);
                                if(busquedaClass.size()>0) {
                                    //Carga la variable de traducción
                                    classNameEsp = busquedaClass.get(0).getTextEsp();
                                }
                                //Buscamos la traducción de la subclase abreviada
                                List<TypeTableEModel> busquedaSubclass = TypeTableERepository.busquedaSubclassShortnameEsp(className, subclassShortName);
                                if(busquedaSubclass.size()>0) {
                                    //Carga la variable de traducción
                                    subclassShortNameEsp = busquedaSubclass.get(0).getShortNameEsp();
                                }
                                jsonEsp = buscarActDocF(jsonEsp, importTable, listaDatosEsp, dato, source, sourceEsp, text, textEsp, 
                                    classSource, classSourceEsp, className , classNameEsp, level, subclassSource, subclassSourceEsp,
                                    subclassShortName, subclassShortNameEsp);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Documento original no encontrado");
                    }
                }
                //Comprobamos si se ha añadido algo al original
                if(!jsonEspOriginal.equals(jsonEsp)) {
                    //Actualiza el documento
                    try (Writer writer = new FileWriter(rutaDocEsp)) {
                        Gson gsonCreate = new GsonBuilder().serializeNulls().create();
                        gsonCreate.toJson(jsonEsp, writer);
                    }
                    System.out.println("Documento "+importTable.getJsonDocument()+" guardado.");
                }
                System.out.println("Fin - Proceso de carga de la tabla "+importTable.getTableId());
            }
        }

        for (ImportTableModel importTable : listaTablas) {
            if(importTable.getTypeTable().equals("A") || importTable.getTypeTable().equals("B")) {
                List<TypeTableAModel> consultaTabla = TypeTableARepository.consulta(importTable.getTableId());
                for (TypeTableAModel registro : consultaTabla) {
                    if(!foundMap.containsKey(importTable.getTableId()+"|"+registro.getText()+"|"+registro.getSource())) {
                        TypeTableARepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText());
                        System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                    }
                }
            } else if(importTable.getTypeTable().equals("C")) {
                List<TypeTableCModel> consultaTabla = TypeTableCRepository.consulta(importTable.getTableId());
                for (TypeTableCModel registro : consultaTabla) {
                    if(!foundMap.containsKey(importTable.getTableId()+"|"+registro.getText()+"|"+registro.getSource()+"|"+registro.getRaceSource()+"|"+registro.getRaceName())) {
                        TypeTableCRepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText(), registro.getRaceSource(), registro.getRaceName());
                        System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                    }
                }
            } else if(importTable.getTypeTable().equals("D")) {
                List<TypeTableDModel> consultaTabla = TypeTableDRepository.consulta(importTable.getTableId());
                for (TypeTableDModel registro : consultaTabla) {
                    if(!foundMap.containsKey(importTable.getTableId()+"|"+registro.getText()+"|"+registro.getSource()+"|"+registro.getClassSource()+"|"
                            +registro.getClassName()+"|"+registro.getLevel())) {
                        TypeTableDRepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText(), registro.getClassSource(),
                            registro.getClassName(), registro.getLevel());
                        System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                    }
                }
            } else if(importTable.getTypeTable().equals("E")) {
                List<TypeTableEModel> consultaTabla = TypeTableERepository.consulta(importTable.getTableId());
                for (TypeTableEModel registro : consultaTabla) {
                    if(!foundMap.containsKey(importTable.getTableId()+"|"+registro.getText()+"|"+registro.getSource()+"|"+registro.getClassSource()+"|"
                            +registro.getClassName()+"|"+registro.getShortName())) {
                        TypeTableERepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText(), registro.getClassSource(),
                            registro.getClassName(), registro.getShortName());
                        System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                    }
                }
            } else if(importTable.getTypeTable().equals("F")) {
                List<TypeTableFModel> consultaTabla = TypeTableFRepository.consulta(importTable.getTableId());
                for (TypeTableFModel registro : consultaTabla) {
                    if(!foundMap.containsKey(importTable.getTableId()+"|"+registro.getText()+"|"+registro.getSource()+"|"+registro.getClassSource()+"|"
                            +registro.getClassName()+"|"+registro.getLevel()+"|"+registro.getSubclassShortName())) {
                        TypeTableFRepository.borrar(importTable.getTableId(), registro.getSource(), registro.getText(), registro.getClassSource(),
                            registro.getClassName(), registro.getLevel(), registro.getSubclassSource(), registro.getSubclassShortName());
                        System.out.println("Registro '"+registro.getText()+"' de "+registro.getSource()+" borrado de la tabla "+importTable.getTableId());
                    }
                }
            }
        }

        System.out.println("--- Fin - Carga de tablas");
    }

    private static JsonObject buscarActDocA(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            HashMap<String,JsonObject> fluffMap, JsonObject dato, String source, String sourceEsp, String text, String textEsp) throws IOException {
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
            dato = actualizarFluff(importTable, fluffMap, dato, source, text);
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject buscarActDocB(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            HashMap<String,JsonObject> fluffMap, JsonObject dato, String source, String sourceEsp, String text, String textEsp) throws IOException {
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
            dato = actualizarFluff(importTable, fluffMap, dato, source, text);
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject buscarActDocC(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            HashMap<String,JsonObject> fluffMap, JsonObject dato, String source, String sourceEsp, String text, String textEsp,
            String raceSource, String raceSourceEsp, String raceName , String raceNameEsp) throws IOException {
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            String sourceDocEsp = datoEsp.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            String raceSourceDocEsp = datoEsp.get("raceSource").getAsString();
            String raceNameDocEsp = datoEsp.get("raceName").getAsString();
            if((sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) &&
                    (raceSourceDocEsp.equals(raceSourceEsp) &&
                    (raceNameDocEsp.equals(raceName) ||
                    raceNameDocEsp.equals(raceNameEsp)))) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            dato.addProperty("raceSource", raceSourceEsp);
            if(!raceNameEsp.isEmpty()) {
                dato.addProperty("raceName", raceNameEsp);
            }
            //Comprobamos si esta tabla puede tener fluff
            if(dato.has("hasFluff") || dato.has("hasFluffImages")){
                dato.remove("hasFluff");
                dato.remove("hasFluffImages");
                
                //Buscamos el identificador en la lista de fluff
                String keyFluff = importTable.getTableId()+"|"+raceName+" ("+text+")"+"|"+source;
                if(fluffMap.containsKey(keyFluff)) {
                    JsonObject datoFluff = fluffMap.get(keyFluff);
                    datoFluff.remove("name");
                    datoFluff.remove("source");
                    //Comprobamos si debe buscar el fluff de su raza
                    if(datoFluff.has("_copy")) {
                        String keyFluffRace = importTable.getTableId()+"|"+raceName+"|"+raceSource;
                        if(fluffMap.containsKey(keyFluffRace)) {
                            JsonObject datoFluffRace = fluffMap.get(keyFluffRace).deepCopy();
                            datoFluffRace.remove("name");
                            datoFluffRace.remove("source");
                            if(datoFluff.get("_copy").getAsJsonObject().has("_mod")) {
                                JsonObject datoFluffCopy = datoFluff.get("_copy").getAsJsonObject().get("_mod").getAsJsonObject();
                                //Comprobamos primero entries
                                if(datoFluffCopy.has("entries")) {
                                    JsonObject datoFluffEntries = datoFluffCopy.get("entries").getAsJsonObject();
                                    //Comprobamos si tiene el código para añadir más parrafos
                                    if(datoFluffEntries.has("mode") && datoFluffEntries.has("items")) {
                                        JsonObject datoFluffNuevoTexto = datoFluffEntries.get("items").getAsJsonObject();
                                        JsonArray listaEntriesFinal = new JsonArray();
                                        //Comprobamos el orden
                                        if(datoFluffEntries.get("mode").getAsString().equals("prependArr")) {
                                            listaEntriesFinal.add(datoFluffNuevoTexto);
                                            //Comprobamos que el fluff de la raza tiene el tipo entries antes de añadir la parte nueva
                                            if(datoFluffRace.has("entries")) {
                                                JsonArray listaEntries = datoFluffRace.get("entries").getAsJsonArray();
                                                listaEntriesFinal.addAll(listaEntries);
                                            }
                                        } else if(datoFluffEntries.get("mode").getAsString().equals("appendArr")) {
                                            //Comprobamos que el fluff de la raza tiene el tipo entries antes de añadir la parte nueva
                                            if(datoFluffRace.has("entries")) {
                                                listaEntriesFinal = datoFluffRace.get("entries").getAsJsonArray();
                                            }
                                            listaEntriesFinal.add(datoFluffNuevoTexto);
                                        } else {
                                            listaEntriesFinal.add(datoFluffNuevoTexto);
                                        }
                                        //Guardamos la nueva lista de fluff
                                        datoFluffRace.add("entries",listaEntriesFinal);
                                    } else {
                                        //Guardamos el fluff la subraza
                                        datoFluffRace.add("entries", datoFluffEntries);
                                    }
                                }
                                //Comprobamos si tiene imagenes
                                if(datoFluffCopy.has("images")) {
                                    JsonObject datoFluffImages = datoFluffCopy.get("images").getAsJsonObject();
                                    //Comprobamos si tiene el código para añadir más imagenes
                                    if(datoFluffImages.has("mode") && datoFluffImages.has("items")) {
                                        JsonObject datoFluffNuevaImg = datoFluffImages.get("items").getAsJsonObject();
                                        JsonArray listaEntriesFinal = new JsonArray();
                                        //Comprobamos el orden
                                        if(datoFluffImages.get("mode").getAsString().equals("prependArr")) {
                                            listaEntriesFinal.add(datoFluffNuevaImg);
                                            //Comprobamos que el fluff de la raza tiene el tipo entries antes de añadir la parte nueva
                                            if(datoFluffRace.has("images")) {
                                                JsonArray listaImages = datoFluffRace.get("images").getAsJsonArray();
                                                listaEntriesFinal.addAll(listaImages);
                                            }
                                        } else if(datoFluffImages.get("mode").getAsString().equals("appendArr")) {
                                            //Comprobamos que el fluff de la raza tiene el tipo entries antes de añadir la parte nueva
                                            if(datoFluffRace.has("images")) {
                                                listaEntriesFinal = datoFluffRace.get("images").getAsJsonArray();
                                            }
                                            listaEntriesFinal.add(datoFluffNuevaImg);
                                        } else {
                                            listaEntriesFinal.add(datoFluffNuevaImg);
                                        }
                                        //Guardamos la nueva lista de fluff
                                        datoFluffRace.add("images",listaEntriesFinal);
                                    } else {
                                        //Guardamos el fluff de la subraza
                                        datoFluffRace.add("images", datoFluffImages);
                                    }
                                }
                            }
                            datoFluff = datoFluffRace;
                        }
                    }
                    //Comprueba si la raza tiene la etiqueta especial de uncommon
                    if(datoFluff.has("uncommon") && fluffMap.containsKey("raceFluffMeta|uncommon")) {
                        datoFluff.remove("uncommon");
                        JsonObject datoRaceFluffMeta = fluffMap.get("raceFluffMeta|uncommon").deepCopy();
                        //Comprueba si ya tiene una etiqueta entries o si tienes que añadirla
                        JsonArray listaEntries = new JsonArray();
                        if(datoFluff.has("entries")) {
                            listaEntries = datoFluff.get("entries").getAsJsonArray();
                        }
                        listaEntries.add(datoRaceFluffMeta);
                        datoFluff.add("entries", listaEntries);
                    }
                    //Comprueba si la raza tiene la etiqueta especial de monstrous
                    if(datoFluff.has("monstrous") && fluffMap.containsKey("raceFluffMeta|monstrous")) {
                        datoFluff.remove("monstrous");
                        JsonObject datoRaceFluffMeta = fluffMap.get("raceFluffMeta|monstrous").deepCopy();
                        //Comprueba si ya tiene una etiqueta entries o si tienes que añadirla
                        JsonArray listaEntries = new JsonArray();
                        if(datoFluff.has("entries")) {
                            listaEntries = datoFluff.get("entries").getAsJsonArray();
                        }
                        listaEntries.add(datoRaceFluffMeta);
                        datoFluff.add("entries", listaEntries);
                    }
                    //Se añade al fluff al dato que se llevará al json
                    dato.add("fluff", datoFluff);
                }
            }
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject buscarActDocD(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            JsonObject dato, String source, String sourceEsp, String text, String textEsp,
            String classSource, String classSourceEsp, String className , String classNameEsp,
            Integer level) throws IOException {
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            String sourceDocEsp = datoEsp.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            String raceSourceDocEsp = datoEsp.get("classSource").getAsString();
            String raceNameDocEsp = datoEsp.get("className").getAsString();
            Integer levelEsp = datoEsp.get("level").getAsInt();
            if((sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) &&
                    (raceSourceDocEsp.equals(classSourceEsp) &&
                    (raceNameDocEsp.equals(className) ||
                    raceNameDocEsp.equals(classNameEsp))) &&
                    levelEsp.equals(level)) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            dato.addProperty("classSource", classSourceEsp);
            if(!classNameEsp.isEmpty()) {
                dato.addProperty("className", classNameEsp);
            }                
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject buscarActDocE(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            JsonObject dato, String source, String sourceEsp, String text, String textEsp,
            String classSource, String classSourceEsp, String className , String classNameEsp,
            String shortName, String shortNameEsp) throws IOException {
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            String sourceDocEsp = datoEsp.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            String classSourceDocEsp = datoEsp.get("classSource").getAsString();
            String classNameDocEsp = datoEsp.get("className").getAsString();
            String shortNameDocEsp = datoEsp.get("shortName").getAsString();
            if((sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) &&
                    (classSourceDocEsp.equals(classSourceEsp) &&
                    (classNameDocEsp.equals(className) ||
                    classNameDocEsp.equals(classNameEsp))) &&
                    (shortNameDocEsp.equals(shortName) ||
                    shortNameDocEsp.equals(shortNameEsp))) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            dato.addProperty("classSource", classSourceEsp);
            if(!classNameEsp.isEmpty()) {
                dato.addProperty("className", classNameEsp);
            }
            if(!shortNameEsp.isEmpty()) {
                dato.addProperty("shortName", shortNameEsp);
            }             
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject buscarActDocF(JsonObject jsonEsp, ImportTableModel importTable, JsonArray listaDatosEsp, 
            JsonObject dato, String source, String sourceEsp, String text, String textEsp,
            String classSource, String classSourceEsp, String className , String classNameEsp,
            Integer level, String subclassSource, String subclassSourceEsp,
            String subclassShortName, String subclassShortNameEsp) throws IOException {
        //Recorre la lista traducida para buscar si ya esta el registro añadido
        boolean encontradoDocEsp = false;
        for (int i = 0; i < listaDatosEsp.size(); i++) {
            JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
            String sourceDocEsp = datoEsp.get("source").getAsString();
            String textDocEsp = datoEsp.get("name").getAsString();
            String classSourceDocEsp = datoEsp.get("classSource").getAsString();
            String classNameDocEsp = datoEsp.get("className").getAsString();
            Integer levelEsp = datoEsp.get("level").getAsInt();
            String subclassSourceDocEsp = datoEsp.get("subclassSource").getAsString();
            String subclassShortNameDocEsp = datoEsp.get("subclassShortName").getAsString();
            if((sourceDocEsp.equals(sourceEsp) &&
                    (textDocEsp.equals(text) ||
                    textDocEsp.equals(textEsp))) &&
                    (classSourceDocEsp.equals(classSourceEsp) &&
                    (classNameDocEsp.equals(className) ||
                    classNameDocEsp.equals(classNameEsp))) &&
                    levelEsp.equals(level) &&
                    (subclassSourceDocEsp.equals(subclassSource) ||
                    subclassSourceDocEsp.equals(subclassSourceEsp)) &&
                    (subclassShortNameDocEsp.equals(subclassShortName) ||
                    subclassShortNameDocEsp.equals(subclassShortNameEsp))) {
                encontradoDocEsp = true;
                i = listaDatosEsp.size();
            }
        }
        //Si no lo encuentra lo añade al documento
        if(!encontradoDocEsp) {
            dato.addProperty("source", sourceEsp);
            dato.addProperty("classSource", classSourceEsp);
            if(!classNameEsp.isEmpty()) {
                dato.addProperty("className", classNameEsp);
            }
            dato.addProperty("subclassSource", subclassSourceEsp);
            if(!subclassShortNameEsp.isEmpty()) {
                dato.addProperty("subclassShortName", subclassShortNameEsp);
            }             
            listaDatosEsp.add(dato);
            jsonEsp.add(importTable.getFieldName(), listaDatosEsp);
            System.out.println("Documento "+importTable.getJsonDocument()+" actualizado, registro: "
                                +text+"|"+source);
        }
        return jsonEsp;
    }

    private static JsonObject actualizarFluff(ImportTableModel importTable, HashMap<String,JsonObject> fluffMap, JsonObject dato, String source, String text) {
        //Comprobamos si tienes token
        if(dato.has("hasToken")) {
            dato.remove("hasToken");
            String rutaToken = "img/";
            if(importTable.getFieldName().equals("object")) {
                rutaToken = rutaToken+"objects/tokens/";
            } else if(importTable.getFieldName().equals("vehicle")) {
                rutaToken = rutaToken+"vehicles/tokens/";
            }
            rutaToken = rutaToken+source+"/";
            String nombreToken = text.trim().replaceAll(" ", "%20")+".png";
            String urlToken = AppProperties.getInstance().getProperty("urlToken")+rutaToken+nombreToken;
            dato.addProperty("tokenUrl", urlToken);
        }
        //Comprobamos si esta tabla puede tener fluff
        if(dato.has("hasFluff") || dato.has("hasFluffImages")){
            dato.remove("hasFluff");
            dato.remove("hasFluffImages");
            
            //Buscamos el identificador en la lista de fluff
            String keyFluff = importTable.getTableId()+"|"+text+"|"+source;
            if(fluffMap.containsKey(keyFluff)) {
                JsonObject datoFluff = fluffMap.get(keyFluff);
                datoFluff.remove("name");
                datoFluff.remove("source");
                //Comprueba si la raza tiene la etiqueta especial de uncommon
                if(datoFluff.has("uncommon") && fluffMap.containsKey("raceFluffMeta|uncommon")) {
                    datoFluff.remove("uncommon");
                    JsonObject datoRaceFluffMeta = fluffMap.get("raceFluffMeta|uncommon").deepCopy();
                    //Comprueba si ya tiene una etiqueta entries o si tienes que añadirla
                    JsonArray listaEntries = new JsonArray();
                    if(datoFluff.has("entries")) {
                        listaEntries = datoFluff.get("entries").getAsJsonArray();
                    }
                    listaEntries.add(datoRaceFluffMeta);
                    datoFluff.add("entries", listaEntries);
                }
                //Comprueba si la raza tiene la etiqueta especial de monstrous
                if(datoFluff.has("monstrous") && fluffMap.containsKey("raceFluffMeta|monstrous")) {
                    datoFluff.remove("monstrous");
                    JsonObject datoRaceFluffMeta = fluffMap.get("raceFluffMeta|monstrous").deepCopy();
                    //Comprueba si ya tiene una etiqueta entries o si tienes que añadirla
                    JsonArray listaEntries = new JsonArray();
                    if(datoFluff.has("entries")) {
                        listaEntries = datoFluff.get("entries").getAsJsonArray();
                    }
                    listaEntries.add(datoRaceFluffMeta);
                    datoFluff.add("entries", listaEntries);
                }
                //Se añade al fluff al dato que se llevará al json
                dato.add("fluff", datoFluff);
            }
        }
        return dato;
    }

}
