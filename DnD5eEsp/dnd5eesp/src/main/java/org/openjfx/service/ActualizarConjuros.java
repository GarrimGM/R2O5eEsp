package org.openjfx.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjfx.AppProperties;
import org.openjfx.repositories.ImportTableRepository;
import org.openjfx.repositories.OptionalFeatureTypesRepository;
import org.openjfx.repositories.SourcesRepository;
import org.openjfx.repositories.TypeTableARepository;
import org.openjfx.repositories.TypeTableCRepository;
import org.openjfx.repositories.TypeTableERepository;
import org.openjfx.repositories.model.ImportTableModel;
import org.openjfx.repositories.model.OptionalFeatureTypesModel;
import org.openjfx.repositories.model.SourcesModel;
import org.openjfx.repositories.model.TypeTableAModel;
import org.openjfx.repositories.model.TypeTableCModel;
import org.openjfx.repositories.model.TypeTableEModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ActualizarConjuros extends Service<Void>{
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                actualizarConjuros();
                return null;
            }
        };
    }

    private static void actualizarConjuros() throws IOException {
        System.out.println("--- Inicio - Actualizar conjuros");

        //Consulta la lista de sources para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,SourcesModel> sourcesMap = new HashMap<>();
        List<SourcesModel> listaSources = SourcesRepository.consultaActivos();
        for (SourcesModel source : listaSources) {
            sourcesMap.put(source.getSource(), source);
        }

        //Consulta la lista de clases para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> classesMap = new HashMap<>();
        List<TypeTableAModel> listaClasses = TypeTableARepository.consulta("Class");
        for (TypeTableAModel clase : listaClasses) {
            classesMap.put(clase.getText(), clase);
        }

        //Consulta la lista de subclases para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableEModel> subClassesMap = new HashMap<>();
        List<TypeTableEModel> listaSubClasses = TypeTableERepository.consulta("Subclass");
        for (TypeTableEModel subClass : listaSubClasses) {
            subClassesMap.put(subClass.getClassName()+"-"+subClass.getShortName(), subClass);
        }

        //Consulta la lista de backgrounds para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> backgroundsMap = new HashMap<>();
        List<TypeTableAModel> listaBackgrounds = TypeTableARepository.consulta("Background");
        for (TypeTableAModel background : listaBackgrounds) {
            backgroundsMap.put(background.getSource()+"-"+background.getText(), background);
        }

        //Consulta la lista de feats para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> featsMap = new HashMap<>();
        List<TypeTableAModel> listaFeats = TypeTableARepository.consulta("Feat");
        for (TypeTableAModel feat : listaFeats) {
            featsMap.put(feat.getSource()+"-"+feat.getText(), feat);
        }

        //Consulta la lista de optionalFeatures para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> optionalFeaturesMap = new HashMap<>();
        List<TypeTableAModel> listaOptionalFeatures = TypeTableARepository.consulta("Optionalfeature");
        for (TypeTableAModel optionalFeature : listaOptionalFeatures) {
            optionalFeaturesMap.put(optionalFeature.getSource()+"-"+optionalFeature.getText(), optionalFeature);
        }

        //Consulta la lista de optionalFeatureTypes para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,OptionalFeatureTypesModel> optionalFeatureTypesMap = new HashMap<>();
        List<OptionalFeatureTypesModel> listaOptionalFeatureTypes = OptionalFeatureTypesRepository.consulta();
        for (OptionalFeatureTypesModel optionalFeatureType : listaOptionalFeatureTypes) {
            optionalFeatureTypesMap.put(optionalFeatureType.getFeatureType(), optionalFeatureType);
        }

        //Consulta la lista de razas para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> racesMap = new HashMap<>();
        List<TypeTableAModel> listaRaces = TypeTableARepository.consulta("Race");
        for (TypeTableAModel race : listaRaces) {
            racesMap.put(race.getSource()+"-"+race.getText(), race);
        }

        //Consulta la lista de subrazas para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableCModel> subRacesMap = new HashMap<>();
        List<TypeTableCModel> listaSubRaces = TypeTableCRepository.consulta("Subrace");
        for (TypeTableCModel subRace : listaSubRaces) {
            subRacesMap.put(subRace.getSource()+"-"+subRace.getRaceName()+" ("+subRace.getText()+")", subRace);
        }

        //Consulta la lista de conjuros para comparar
        //creo un hashmpap para solo tener que recorrer la lista una vez
        HashMap<String,TypeTableAModel> spellsMap = new HashMap<>();
        List<TypeTableAModel> listaSpells = TypeTableARepository.consulta("Spell");
        for (TypeTableAModel spell : listaSpells) {
            spellsMap.put(spell.getSource()+"-"+spell.getTextEsp(), spell);
        }

        //Cogemos el fichero original de relacciones de conjuros y lo convertimos a un json con el que podamos trabajar
        String rutaSpellsSources = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\generated\\gendata-spell-source-lookup.json";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(rutaSpellsSources));
        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject jsonSpellSource = gson.fromJson(bufferedReader, JsonObject.class);
        Map<String,JsonElement> jsonSpellSourceMap = jsonSpellSource.asMap();
        
        //Recuperamos la lista de documentos json con conjuros
        List<ImportTableModel> listaDocuSpells = ImportTableRepository.consultaDocumentSpellsActivos();
        for (ImportTableModel documentSpells : listaDocuSpells) {
            //Con el nombre del documento sacamos el source
            String jsonDocument = documentSpells.getJsonDocument();
            String rutaJsonDocumentEsp = AppProperties.getInstance().getProperty("rutaEsp")+"data\\"+jsonDocument;
            String rutaJsonDocumentOriginal = AppProperties.getInstance().getProperty("rutaOriginal")+"data\\"+jsonDocument;
            String sourceDocument = jsonDocument.substring(jsonDocument.indexOf("-")+1, jsonDocument.indexOf("."));

            //Recuperamos el Json de spells del source de la iteracion
            BufferedReader bufferedReaderEsp = new BufferedReader(new FileReader(rutaJsonDocumentEsp));
            Gson gsonEsp = new GsonBuilder().serializeNulls().create();
            JsonObject jsonDocumentEsp = gsonEsp.fromJson(bufferedReaderEsp, JsonObject.class);

            BufferedReader bufferedReaderOriginal = new BufferedReader(new FileReader(rutaJsonDocumentOriginal));
            Gson gsonOriginal = new GsonBuilder().serializeNulls().create();
            JsonObject jsonDocumentOriginal = gsonOriginal.fromJson(bufferedReaderOriginal, JsonObject.class);
            JsonArray jsonSpellsListaOriginal = jsonDocumentOriginal.getAsJsonArray("spell");
            HashMap<String,JsonObject> jsonSpellsMapOriginal = new HashMap<>();
            for (JsonElement jsonSpellOriginal : jsonSpellsListaOriginal) {
                jsonSpellsMapOriginal.put(jsonSpellOriginal.getAsJsonObject().get("name").getAsString(), jsonSpellOriginal.getAsJsonObject());
            }
            
            //Buscamos el source en el documento con la relacciones
            if(jsonSpellSourceMap.containsKey(sourceDocument)){
                JsonArray jsonSpellsListaEsp = jsonDocumentEsp.getAsJsonArray("spell");

                JsonObject jsonSource = jsonSpellSourceMap.get(sourceDocument).getAsJsonObject();
                Map<String,JsonElement> jsonSourceMap = jsonSource.asMap();

                for (JsonElement jsonSpell : jsonSpellsListaEsp) {
                    //Borramos la configuracion antes de colocar la nueva
                    jsonSpell.getAsJsonObject().remove("miscTags");
                    jsonSpell.getAsJsonObject().remove("areaTags");
                    jsonSpell.getAsJsonObject().remove("classes");
                    jsonSpell.getAsJsonObject().remove("races");
                    jsonSpell.getAsJsonObject().remove("backgrounds");
                    jsonSpell.getAsJsonObject().remove("feats");

                    //Buscamos el nombre en ingles
                    String nombreSpellOriginal = spellsMap.get(sourceDocument.toUpperCase()+"-"+jsonSpell.getAsJsonObject().get("name").getAsString()).getText();
                    JsonObject spellOriginal = jsonSpellsMapOriginal.get(nombreSpellOriginal);

                    //miscTags
                    if(spellOriginal.has("miscTags")) {
                        jsonSpell.getAsJsonObject().add("miscTags", spellOriginal.get("miscTags"));
                    }

                    //areaTags
                    if(spellOriginal.has("areaTags")) {
                        jsonSpell.getAsJsonObject().add("areaTags", spellOriginal.get("areaTags"));
                    }

                    //Continuamos con las clases, razas, backgroups y feats, van todos juntos
                    if(jsonSourceMap.containsKey(nombreSpellOriginal.toLowerCase())) {
                        JsonObject spellSource = jsonSourceMap.get(nombreSpellOriginal.toLowerCase()).getAsJsonObject();
                        JsonObject classes = new JsonObject();
                        boolean tieneClase = false;
                        JsonArray backgrounds = new JsonArray();
                        JsonArray feats = new JsonArray();
                        JsonArray races = new JsonArray(); 
                        JsonArray optionalfeatures = new JsonArray(); 
                        if(spellSource.has("class")) {
                            JsonObject claseSources = spellSource.getAsJsonObject("class");
                            Map<String, JsonElement> claseSourcesMap = claseSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : claseSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreClase = entryClass.getKey();
                                        if(classesMap.containsKey(nombreClase)) {
                                            TypeTableAModel claseTabla = classesMap.get(nombreClase);
                                            if(!classes.has("fromClassList")) {
                                                classes.add("fromClassList", new JsonArray());
                                            }
                                            JsonObject infoClase = new JsonObject();
                                            infoClase.addProperty("name", claseTabla.getTextEsp());
                                            infoClase.addProperty("source", source+"-ES");
                                            classes.getAsJsonArray("fromClassList").add(infoClase);
                                            tieneClase = true;
                                        }
                                    }
                                }
                            }
                        }
                        if(spellSource.has("classVariant")) {
                            JsonObject claseVariantSources = spellSource.getAsJsonObject("classVariant");
                            Map<String, JsonElement> claseVariantSourcesMap = claseVariantSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : claseVariantSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClassVariant : objetoMap.entrySet()) {
                                        String nombreClase = entryClassVariant.getKey();
                                        if(classesMap.containsKey(nombreClase)) {
                                            String sourceDef = entryClassVariant.getValue().getAsJsonObject().getAsJsonArray("definedInSources").get(0).getAsString();
                                            if(sourcesMap.containsKey(sourceDef)) {
                                                TypeTableAModel claseTabla = classesMap.get(nombreClase);
                                                if(!classes.has("fromClassListVariant")) {
                                                    classes.add("fromClassListVariant", new JsonArray());
                                                }
                                                JsonObject infoVariantClase = new JsonObject();
                                                infoVariantClase.addProperty("name", claseTabla.getTextEsp());
                                                infoVariantClase.addProperty("source", source+"-ES");
                                                infoVariantClase.addProperty("definedInSource", sourceDef+"-ES");
                                                classes.getAsJsonArray("fromClassListVariant").add(infoVariantClase);
                                                tieneClase = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(spellSource.has("subclass")) {
                            JsonObject subclaseSources = spellSource.getAsJsonObject("subclass");
                            Map<String, JsonElement> subclaseSourcesMap = subclaseSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : subclaseSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreClase = entryClass.getKey();
                                        if(classesMap.containsKey(nombreClase)) {
                                            TypeTableAModel claseTabla = classesMap.get(nombreClase);
                                            JsonObject objetoSubClass = entryClass.getValue().getAsJsonObject();
                                            Map<String, JsonElement> objetoSubClassMap = objetoSubClass.asMap();
                                            for (Map.Entry<String, JsonElement> entrySubClass : objetoSubClassMap.entrySet()) {
                                                String sourceSubClass = entrySubClass.getKey();
                                                if(sourcesMap.containsKey(sourceSubClass)) {
                                                    JsonObject objetoSubClassFinal = entrySubClass.getValue().getAsJsonObject();
                                                    Map<String, JsonElement> objetoSubClassFinalMap = objetoSubClassFinal.asMap();
                                                    for (Map.Entry<String, JsonElement> entrySubClassNombre : objetoSubClassFinalMap.entrySet()) {
                                                        String nombreSubClase = entrySubClassNombre.getKey();
                                                        if(subClassesMap.containsKey(nombreClase+"-"+nombreSubClase)) {
                                                            TypeTableEModel subClassTabla = subClassesMap.get(nombreClase+"-"+nombreSubClase);
                                                            if(!classes.has("fromSubclass")) {
                                                                classes.add("fromSubclass", new JsonArray());
                                                            }
                                                            JsonObject infoClase = new JsonObject();
                                                            infoClase.addProperty("name", claseTabla.getTextEsp());
                                                            infoClase.addProperty("source", source+"-ES");
                                                            JsonObject infoSubClase = new JsonObject();
                                                            infoSubClase.addProperty("name", subClassTabla.getTextEsp());
                                                            infoSubClase.addProperty("shortName", subClassTabla.getShortNameEsp());
                                                            infoSubClase.addProperty("source", sourceSubClass+"-ES");
                                                            JsonObject infoSubClaseCompleta = new JsonObject();
                                                            infoSubClaseCompleta.add("class", infoClase);
                                                            infoSubClaseCompleta.add("subclass", infoSubClase);
                                                            classes.getAsJsonArray("fromSubclass").add(infoSubClaseCompleta);
                                                            tieneClase = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(tieneClase) {
                            jsonSpell.getAsJsonObject().add("classes", classes);
                        }

                        if(spellSource.has("background")) {
                            JsonObject backgroundSources = spellSource.getAsJsonObject("background");
                            Map<String, JsonElement> backgroundSourcesMap = backgroundSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : backgroundSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreBackground = entryClass.getKey();
                                        if(backgroundsMap.containsKey(source+"-"+nombreBackground)) {
                                            TypeTableAModel backgroundTabla = backgroundsMap.get(source+"-"+nombreBackground);
                                            JsonObject infoBackground = new JsonObject();
                                            infoBackground.addProperty("name", backgroundTabla.getTextEsp());
                                            infoBackground.addProperty("source", source+"-ES");
                                            backgrounds.add(infoBackground);
                                        }
                                    } 
                                }
                            }
                            if(backgrounds.size()>0){
                                jsonSpell.getAsJsonObject().add("backgrounds", backgrounds);
                            }
                        }

                        if(spellSource.has("feat")) {
                            JsonObject featSources = spellSource.getAsJsonObject("feat");
                            Map<String, JsonElement> featSourcesMap = featSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : featSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreFeat = entryClass.getKey();
                                        if(featsMap.containsKey(source+"-"+nombreFeat)) {
                                            TypeTableAModel featTabla = featsMap.get(source+"-"+nombreFeat);
                                            JsonObject infoFeat = new JsonObject();
                                            infoFeat.addProperty("name", featTabla.getTextEsp());
                                            infoFeat.addProperty("source", source+"-ES");
                                            feats.add(infoFeat);
                                        }
                                    } 
                                }
                            }
                            if(feats.size()>0){
                                jsonSpell.getAsJsonObject().add("feats", feats);
                            }
                        }

                        if(spellSource.has("race")) {
                            JsonObject raceSources = spellSource.getAsJsonObject("race");
                            Map<String, JsonElement> raceSourcesMap = raceSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : raceSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreRaceCompleto = entryClass.getKey();
                                        //Detectamos las subrazas si tienes un parentesis
                                        int indiceSubRace = nombreRaceCompleto.indexOf("(");
                                        if(indiceSubRace>=0) {
                                            if(subRacesMap.containsKey(source+"-"+nombreRaceCompleto)) {
                                                String nameRace = entryClass.getValue().getAsJsonObject().get("baseName").getAsString();
                                                String sourceRace = entryClass.getValue().getAsJsonObject().get("baseSource").getAsString();
                                                TypeTableAModel raceTabla = racesMap.get(sourceRace+"-"+nameRace);
                                                TypeTableCModel subRaceTabla = subRacesMap.get(source+"-"+nombreRaceCompleto);
                                                JsonObject infoSubRace = new JsonObject();
                                                infoSubRace.addProperty("name", raceTabla.getTextEsp()+" ("+subRaceTabla.getTextEsp()+")");
                                                infoSubRace.addProperty("source", source+"-ES");
                                                infoSubRace.addProperty("baseName", raceTabla.getTextEsp());
                                                infoSubRace.addProperty("baseSource", sourceRace+"-ES");
                                                races.add(infoSubRace);
                                            }
                                        } else {
                                            if(racesMap.containsKey(source+"-"+nombreRaceCompleto)) {
                                                TypeTableAModel raceTabla = racesMap.get(source+"-"+nombreRaceCompleto);
                                                JsonObject infoRace = new JsonObject();
                                                infoRace.addProperty("name", raceTabla.getTextEsp());
                                                infoRace.addProperty("source", source+"-ES");
                                                races.add(infoRace);
                                            }
                                        }
                                    }
                                }
                            }
                            if(races.size()>0){
                                jsonSpell.getAsJsonObject().add("races", races);
                            }
                        }

                        if(spellSource.has("optionalfeature")) {
                            JsonObject optFeatureSources = spellSource.getAsJsonObject("optionalfeature");
                            Map<String, JsonElement> optFeatureSourcesMap = optFeatureSources.asMap();
                            for (Map.Entry<String, JsonElement> entry : optFeatureSourcesMap.entrySet()) {
                                String source = entry.getKey();
                                if(sourcesMap.containsKey(source)) {
                                    JsonObject objeto = entry.getValue().getAsJsonObject();
                                    Map<String, JsonElement> objetoMap = objeto.asMap();
                                    for (Map.Entry<String, JsonElement> entryClass : objetoMap.entrySet()) {
                                        String nombreOptFeature = entryClass.getKey();
                                        if(optionalFeaturesMap.containsKey(source+"-"+nombreOptFeature)) {
                                            TypeTableAModel optFeatureTabla = optionalFeaturesMap.get(source+"-"+nombreOptFeature);
                                            JsonObject infoOptFeature = new JsonObject();
                                            infoOptFeature.addProperty("name", optFeatureTabla.getTextEsp());
                                            infoOptFeature.addProperty("source", source+"-ES");
                                            infoOptFeature.add("featureType", new JsonArray());
                                            JsonArray listaFeatureType = entryClass.getValue().getAsJsonObject().getAsJsonArray("featureType");
                                            for (JsonElement featureType : listaFeatureType) {
                                                if(optionalFeatureTypesMap.containsKey(featureType.getAsString())) {
                                                    infoOptFeature.get("featureType").getAsJsonArray().add(optionalFeatureTypesMap.get(featureType.getAsString()).getFeatureTypeEsp());
                                                }
                                            } 
                                            optionalfeatures.add(infoOptFeature);
                                        }
                                    } 
                                }
                            }
                            if(optionalfeatures.size()>0){
                                jsonSpell.getAsJsonObject().add("optionalfeatures", optionalfeatures);
                            }
                        }
                    }
                }
            }

            //Crea el documento físico
            try (Writer writer = new FileWriter(rutaJsonDocumentEsp)) {
                gson.toJson(jsonDocumentEsp, writer);
            }
        }

        System.out.println("--- Fin - Actualizar conjuros");
    }
}
