package org.openjfx;

import java.io.IOException;

import org.openjfx.service.CargarTablas;
import org.openjfx.service.FormarDocumento;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void cargarTablas() throws IOException {
        CargarTablas.cargarTablas();
    }

    @FXML
    private void formarDocumento() throws IOException {
        FormarDocumento.crear();
    }
}
