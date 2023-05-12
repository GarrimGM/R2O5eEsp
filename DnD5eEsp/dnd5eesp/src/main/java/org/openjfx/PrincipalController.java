package org.openjfx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.openjfx.service.CargarTablas;
import org.openjfx.service.FormarDocumento;
import org.openjfx.service.OrdenarDocumentos;

import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;

public class PrincipalController implements Initializable {

    private CargarTablas cargarTablasService;
    private FormarDocumento formarDocumentoService;
    private OrdenarDocumentos ordenarDocumentosOriginalService;
    private OrdenarDocumentos ordenarDocumentosInglesService;

    //Botones
    @FXML
    private Button cargarTablas;
    @FXML
    private Button actualizarConjuros;
    @FXML
    private Button ordenar;
    @FXML
    private Button ordenarOriginal;
    @FXML
    private Button formarDocumento;

    //Overlay
    @FXML
    private Pane overlay;

    //Antiestres
    @FXML
    private ProgressIndicator progressIndicator;

    public void showOverlay(boolean show) {
        overlay.setVisible(show);
        progressIndicator.setVisible(show);
    }

    //Eventos de los botones
    @FXML
    private void cargarTablas() throws IOException {
        showOverlay(true);
        cargarTablasService.reset();
        cargarTablasService.start();
        showOverlay(false);
    }

    @FXML
    private void formarDocumento() throws IOException {
        formarDocumentoService.reset();
        formarDocumentoService.start();
    }

    @FXML
    private void ordenar() throws IOException {
        ordenarDocumentosInglesService.reset();
        ordenarDocumentosInglesService.start();
    }

    @FXML
    private void ordenarOriginal() throws IOException {
        ordenarDocumentosOriginalService.reset();
        ordenarDocumentosOriginalService.start();
    }

    public void initialize() {
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {        

        // Crear los servicios
        cargarTablasService = new CargarTablas();
        cargarTablasService.setOnRunning(event -> {
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
        cargarTablasService.setOnSucceeded(event -> {
            progressIndicator.setProgress(1.0);
        });
        cargarTablasService.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == State.SUCCEEDED || newState == State.CANCELLED || newState == State.FAILED) {
                activarBotones();
            } else if (newState == State.RUNNING) {
                desactivarBotones();
            }
        });


        formarDocumentoService = new FormarDocumento();
        formarDocumentoService.setOnRunning(event -> {
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
        formarDocumentoService.setOnSucceeded(event -> {
            progressIndicator.setProgress(1.0);
        });
        formarDocumentoService.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == State.SUCCEEDED || newState == State.CANCELLED || newState == State.FAILED) {
                activarBotones();
            } else if (newState == State.RUNNING) {
                desactivarBotones();
            }
        });


        ordenarDocumentosOriginalService = new OrdenarDocumentos(true);
        ordenarDocumentosOriginalService.setOnRunning(event -> {
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
        ordenarDocumentosOriginalService.setOnSucceeded(event -> {
            progressIndicator.setProgress(1.0);
        });
        ordenarDocumentosOriginalService.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == State.SUCCEEDED || newState == State.CANCELLED || newState == State.FAILED) {
                activarBotones();
            } else if (newState == State.RUNNING) {
                desactivarBotones();
            }
        });


        ordenarDocumentosInglesService = new OrdenarDocumentos(false);
        ordenarDocumentosInglesService.setOnRunning(event -> {
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
        ordenarDocumentosInglesService.setOnSucceeded(event -> {
            progressIndicator.setProgress(1.0);
        });
        ordenarDocumentosInglesService.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == State.SUCCEEDED || newState == State.CANCELLED || newState == State.FAILED) {
                activarBotones();
            } else if (newState == State.RUNNING) {
                desactivarBotones();
            }
        });
    }

    private void desactivarBotones() {
        cargarTablas.setDisable(true);
        actualizarConjuros.setDisable(true);
        ordenar.setDisable(true);
        ordenarOriginal.setDisable(true);
        formarDocumento.setDisable(true);
    }

    private void activarBotones() {
        cargarTablas.setDisable(false);
        actualizarConjuros.setDisable(false);
        ordenar.setDisable(false);
        ordenarOriginal.setDisable(false);
        formarDocumento.setDisable(false);
    }
}
