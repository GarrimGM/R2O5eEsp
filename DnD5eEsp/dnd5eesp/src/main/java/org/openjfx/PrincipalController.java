package org.openjfx;

import java.io.IOException;

import org.openjfx.service.CargarTablas;
import org.openjfx.service.FormarDocumento;
import org.openjfx.service.OrdenarDocumentos;

import javafx.fxml.FXML;

public class PrincipalController {

    @FXML
    private void cargarTablas() throws IOException {
        CargarTablas.cargarTablas();
    }

    @FXML
    private void formarDocumento() throws IOException {
        FormarDocumento.crear();
    }

    @FXML
    private void ordenar() throws IOException {
        OrdenarDocumentos.ordenar(false);
    }

    @FXML
    private void ordenarOriginal() throws IOException {
        OrdenarDocumentos.ordenar(true);
    }
}
