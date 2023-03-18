package org.openjfx.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openjfx.AppProperties;
import org.openjfx.repositories.ImportTableRepository;
import org.openjfx.repositories.OptionalFeatureTypesRepository;
import org.openjfx.repositories.SourcesRepository;
import org.openjfx.repositories.model.ImportTableModel;
import org.openjfx.repositories.model.OptionalFeatureTypesModel;
import org.openjfx.repositories.model.SourcesModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FormarDocumento {
    public static void crear() throws IOException {
        System.out.println("Inicio - Creando documento final");
        //Crea el documento json base
        String rutaDocumento = AppProperties.getInstance().getProperty("rutaEsp")+"DnD-ES.json";
        JsonObject jsDocumento =  new JsonObject();

        //Grupo de _Meta
        JsonObject jsMeta = new JsonObject();
        //Sources dentro de _Meta
        JsonArray jsSources = new JsonArray();
        List<SourcesModel> listaSources = SourcesRepository.consulta();
        for (SourcesModel sourcesModel : listaSources) {
            JsonObject jsSource = new JsonObject();
            JsonArray jsAuthors = new JsonArray();
            jsAuthors.add("Grrm");
            JsonArray jsConvertedBy = new JsonArray();
            jsConvertedBy.add("");

            jsSource.addProperty("json", sourcesModel.getSourceEsp());
            jsSource.addProperty("abbreviation", sourcesModel.getSourceEsp());
            jsSource.addProperty("full", sourcesModel.getTextEsp());
            jsSource.addProperty("color", sourcesModel.getColor());
            jsSource.add("authors", jsAuthors);
            jsSource.add("convertedBy", jsConvertedBy);
            jsSource.addProperty("version", "1");
            jsSource.addProperty("url", "");

            jsSources.add(jsSource);
        }
        jsMeta.add("sources", jsSources);

        //OptionalFeatureTypes dentro de _Meta
        JsonObject jsOptionalFeatureTypes = new JsonObject();
        List<OptionalFeatureTypesModel> listaOptionalFeatureTypes = OptionalFeatureTypesRepository.consulta();
        for (OptionalFeatureTypesModel optionalFeatureTypesModel : listaOptionalFeatureTypes) {
            jsOptionalFeatureTypes.addProperty(optionalFeatureTypesModel.getFeatureTypeEsp(), optionalFeatureTypesModel.getTextEsp());
        }
        jsMeta.add("optionalFeatureTypes", jsOptionalFeatureTypes);

        //Informa el grupo de _Meta
        jsDocumento.add("_meta", jsMeta);

        //Consulta la lista de documentos a importar
        List<ImportTableModel> listaTablas = ImportTableRepository.consultaActivos();
        for (ImportTableModel importTable : listaTablas) {
            String rutaDocEsp = AppProperties.getInstance().getProperty("rutaEsp")+"data\\"+importTable.getJsonDocument();

            try {
                BufferedReader bufferedReaderEsp = new BufferedReader(new FileReader(rutaDocEsp));
                Gson gsonEsp = new Gson();
                JsonObject jsonEsp = gsonEsp.fromJson(bufferedReaderEsp, JsonObject.class);
                //Comprueba si tiene el campo
                if(!jsonEsp.has(importTable.getFieldName())){
                    JsonArray jsLista = new JsonArray();
                    jsonEsp.add(importTable.getFieldName(), jsLista);
                }
                JsonArray jsLista = jsonEsp.getAsJsonArray(importTable.getFieldName());
                jsDocumento.add(importTable.getFieldName(), jsLista);
            } catch (FileNotFoundException e) {
                System.out.println("Documento "+importTable.getJsonDocument()+" no encontrado");
            }
        }  


        //Crea el documento físico
        try (Writer writer = new FileWriter(rutaDocumento)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(jsDocumento, writer);
        }

        System.out.println("Fin - Creando documento final");
    }
}
