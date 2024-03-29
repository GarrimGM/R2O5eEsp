package org.openjfx.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class OrdenarDocumentos extends Service<Void> {

    private boolean ordenOriginal;

    public OrdenarDocumentos(boolean ordenOriginal) {
        this.ordenOriginal = ordenOriginal;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ordenar(ordenOriginal);
                return null;
            }
        };
    }

    private static void ordenar(boolean original) throws IOException {
        System.out.println("--- Inicio - Ordenando documentos");
        //Consulta la lista de sources activos para comparar
        //creo un hashmap para solo tener que recorrer esta lista una vez
        HashMap<String,SourcesModel> sourcesMap = new HashMap<>();
        List<SourcesModel> listaSources = SourcesRepository.consultaActivos();
        for (SourcesModel source : listaSources) {
            sourcesMap.put(source.getSource(), source);
        }

        //Consulta la lista de documentos a importar
        List<ImportTableModel> listaTablas = ImportTableRepository.consultaActivos();
        for (ImportTableModel importTable : listaTablas) {
            System.out.println("Inicio - Ordenando documento "+importTable.getJsonDocument());
            //Recupera el json del documento
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
            //Creamos una copia de como estaba inicialmente
            JsonObject jsonEspOriginal = jsonEsp.deepCopy();

            JsonArray jsListaOrdenado = new JsonArray();
            if(original) {
                //Ordenación por los nombres originales en ingles
                HashMap<String,JsonObject> listaDatosEspMap = new HashMap<>();
                JsonArray listaDatosEsp = jsonEsp.get(importTable.getFieldName()).getAsJsonArray();
                for (int i = 0; i < listaDatosEsp.size(); i++) {
                    JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
                    String sourceDocEsp = "";
                    String textDocEsp = "";
                    //Comprobar el tipo de tabla
                    if(importTable.getTypeTable().equals("A")) {
                        sourceDocEsp = datoEsp.get("source").getAsString();
                        textDocEsp = datoEsp.get("name").getAsString();
                    } else if(importTable.getTypeTable().equals("B")) {
                        JsonObject datoEspHeredado = datoEsp.getAsJsonObject("inherits");
                        sourceDocEsp = datoEspHeredado.get("source").getAsString();
                        textDocEsp = datoEsp.get("name").getAsString();
                    }
                    String clave = sourceDocEsp+'|'+textDocEsp;
                    listaDatosEspMap.put(clave, datoEsp);
                }

                List<TypeTableAModel> listaDatos = TypeTableARepository.consulta(importTable.getTableId());
                for (TypeTableAModel dato : listaDatos) {
                    String sourceEsp = sourcesMap.get(dato.getSource()).getSourceEsp();
                    String textoOriginal = dato.getText();
                    String textoTraduc = "";
                    if(dato.getTextEsp()!=null) {
                        textoTraduc = dato.getTextEsp();
                    }
                    String claveOriginal = sourceEsp+'|'+textoOriginal;
                    String claveTraduc = sourceEsp+'|'+textoTraduc;

                    if(textoTraduc!="" && listaDatosEspMap.containsKey(claveTraduc)) {
                        jsListaOrdenado.add(listaDatosEspMap.get(claveTraduc));
                    } else if(listaDatosEspMap.containsKey(claveOriginal)){
                        jsListaOrdenado.add(listaDatosEspMap.get(claveOriginal));
                    }
                }
            } else {
                //Ordenados alfabeticamente por como este en el documento traducido
                List<String> listaOrdenada = new ArrayList<>();
                HashMap<String,JsonObject> listaDatosEspMap = new HashMap<>();

                JsonArray listaDatosEsp = jsonEsp.get(importTable.getFieldName()).getAsJsonArray();
                for (int i = 0; i < listaDatosEsp.size(); i++) {
                    JsonObject datoEsp = listaDatosEsp.get(i).getAsJsonObject();
                    String sourceDocEsp = "";
                    String textDocEsp = "";
                    //Comprobar el tipo de tabla
                    if(importTable.getTypeTable().equals("A")) {
                        sourceDocEsp = datoEsp.get("source").getAsString();
                        textDocEsp = datoEsp.get("name").getAsString();
                    } else if(importTable.getTypeTable().equals("B")) {
                        JsonObject datoEspHeredado = datoEsp.getAsJsonObject("inherits");
                        sourceDocEsp = datoEspHeredado.get("source").getAsString();
                        textDocEsp = datoEsp.get("name").getAsString();
                    }
                    String clave = sourceDocEsp+'|'+textDocEsp+i;
                    listaDatosEspMap.put(clave, datoEsp);
                    listaOrdenada.add(clave);
                }

                Collections.sort(listaOrdenada);
                for (String clave : listaOrdenada) {
                    jsListaOrdenado.add(listaDatosEspMap.get(clave));
                }
            }
            jsonEsp.add(importTable.getFieldName(), jsListaOrdenado);

            //Comprobamos si se ha añadido algo al original
            if(!jsonEspOriginal.equals(jsonEsp)) {
                //Actualiza el documento
                try (Writer writer = new FileWriter(rutaDocEsp)) {
                    Gson gsonCreate = new GsonBuilder().serializeNulls().create();
                    gsonCreate.toJson(jsonEsp, writer);
                }
                System.out.println("Documento "+importTable.getJsonDocument()+" guardado.");
            }
            System.out.println("Fin - Ordenando documento "+importTable.getJsonDocument());
        }
        System.out.println("--- Fin - Ordenando documentos");
    }
}
