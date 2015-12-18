package hu.bme.mit.mihf.bisectkmeans.ui;

import hu.bme.mit.mihf.bisectkmeans.algorithm.Algorithm;
import hu.bme.mit.mihf.bisectkmeans.algorithm.MetricsOptions;
import hu.bme.mit.mihf.bisectkmeans.algorithm.StartVectorOptions;
import hu.bme.mit.mihf.bisectkmeans.inputloader.Loader;
import hu.bme.mit.mihf.bisectkmeans.model.DataModel;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Controller {
    public TextField textFieldLoadFile;
    public TextField textFieldNumberOfClusters;
    public TextField textFieldMinimumIterations;
    public TextField textFieldMaximumCentroidMovement;
    public ToggleButton toggleBtnViewSteps;
    public ComboBox comboBoxMetrics;
    public ComboBox comboBoxStartVector;
    public GridPane rootGridPane;
    public Button btnStartAlgorithm;
    public Button btnNextStep;
    public Button btnStopAlgorithm;
    public Button btnFileChooser;
    public Label labelInfoBar;
    public Canvas canvasDiagram;
    public StackPane stackPaneCanvasHolder;

    private Thread backgroundWorker;

    public void handleWindowShownEvent() {
        canvasDiagram.widthProperty().bind(stackPaneCanvasHolder.widthProperty());
        canvasDiagram.heightProperty().bind(stackPaneCanvasHolder.heightProperty());

        try {
            textFieldLoadFile.setText(new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/')+"/default_input.txt");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        comboBoxStartVector.getItems().setAll(StartVectorOptions.values());
        comboBoxStartVector.setValue(comboBoxStartVector.getItems().get(0));

        comboBoxMetrics.getItems().setAll(MetricsOptions.values());
        comboBoxMetrics.setValue(comboBoxMetrics.getItems().get(0));

        clearDiagram();
    }

    public void handleResizeEvent() {
        clearDiagram();
    }

    public void startAlgorithm(ActionEvent actionEvent) {
        disableParameterControls(true);
        btnNextStep.setDisable(true);

        clearDiagram();

        if (backgroundWorker != null) {
            backgroundWorker.stop();
            backgroundWorker = null;
        }

        startAlgorithmWithLoadingModel();
    }

    private void startAlgorithmWithLoadingModel() {
        labelInfoBar.setText(textFieldLoadFile.getText() + " betöltése...");

        backgroundWorker = new Loader(textFieldLoadFile.getText(), new Loader.FinishedEventHandler() {
            @Override
            public void onLoadedSuccessFully(final DataModel model) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showInitialState(model);
                        labelInfoBar.setText("");
                        backgroundWorker = null;
                        startAlgorithmModelLoaded(model);
                    }
                });
            }

            @Override
            public void onError(final String errorMessage) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showErrorMessage("A betöltés során hiba történt: " + errorMessage);
                    }
                });
            }
        });
        backgroundWorker.start();
    }

    private void startAlgorithmModelLoaded(DataModel model) {
        if (model == null)
            return;

        if (backgroundWorker != null) {
            backgroundWorker.stop();
            backgroundWorker = null;
        }

        int numberOfClusters;

        try {
            numberOfClusters = Integer.parseInt(textFieldNumberOfClusters.getText());
            if (numberOfClusters <= 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e) {
            showErrorMessage("Az osztályok számának pozitív egész számnak kell lennie!");
            return;
        }

        int minimumIterations = 0;
        if (!textFieldMinimumIterations.getText().isEmpty()) {
            try {
                minimumIterations = Integer.parseInt(textFieldMinimumIterations.getText());
                if (minimumIterations < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showErrorMessage("A legkevesbb iteráció számának természetes számnak kell lennie!");
                return;
            }
        }

        double maximumCentroidMovement = -1;
        if (!textFieldMaximumCentroidMovement.getText().isEmpty()) {
            try {
                maximumCentroidMovement = Double.parseDouble(textFieldMaximumCentroidMovement.getText());
                if (maximumCentroidMovement < 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showErrorMessage("A centroidok maximális mozgásának pozítív (vagy 0) racionális számnak kell lennie!");
                return;
            }
        }

        backgroundWorker = new Algorithm(
                (MetricsOptions)comboBoxMetrics.getValue(),
                (StartVectorOptions)comboBoxStartVector.getValue(),
                numberOfClusters,
                minimumIterations,
                maximumCentroidMovement,
                toggleBtnViewSteps.isSelected(),
                model,
                new Algorithm.AlgorithmObserver() {
                    @Override
                    public void algorithmFinished(final ArrayList<DataModel> result) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showResult(result);
                                disableParameterControls(false);
                                labelInfoBar.setText("");
                            }
                        });
                    }

                    @Override
                    public void algorithmPaused(final ArrayList<DataModel> partialResult) {
                        if (toggleBtnViewSteps.isSelected()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    showPartialResult(partialResult);
                                    btnNextStep.setDisable(false);
                                    labelInfoBar.setText("");
                                }
                            });
                        }
                    }

                    @Override
                    public void errorOccurred(final String errorMessage) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showErrorMessage(errorMessage);
                            }
                        });
                    }
                }
        );

        labelInfoBar.setText("Az algoritmus fut...");

        backgroundWorker.start();
    }

    private void clearDiagram() {
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, canvasDiagram.getWidth(), canvasDiagram.getHeight());
    }

    private void showInitialState(DataModel model) {
        clearDiagram();
        Diagramm dg = new Diagramm(canvasDiagram, DataModel.DIMENSION);
        dg.drawDiagramm(model, Color.GREEN, true);
    }

    private void showResult(ArrayList<DataModel> result) {
        clearDiagram();
        Diagramm dg = new Diagramm(canvasDiagram, DataModel.DIMENSION);
        dg.drawDiagramm(result);
    }

    private void showPartialResult(ArrayList<DataModel> partialResult) {
        clearDiagram();
        Diagramm dg = new Diagramm(canvasDiagram, DataModel.DIMENSION);
        dg.drawDiagramm(partialResult);
    }

    private void showErrorMessage(String message) {
        labelInfoBar.setText(message);
        disableParameterControls(false);
    }


    public void onFileChooserClick(ActionEvent actionEvent) {
        File chosenFile = new FileChooser().showOpenDialog(rootGridPane.getScene().getWindow());
        if (chosenFile != null) {
            textFieldLoadFile.setText(chosenFile.getAbsolutePath());
        }
    }

    public void nextStep(ActionEvent actionEvent) {
        try {
            btnNextStep.setDisable(true);
            ((Algorithm)backgroundWorker).nextStep();
            labelInfoBar.setText("Az algoritmus fut...");
        }
        catch (NullPointerException e) {}
        catch (ClassCastException e) {}
    }

    public void stopAlgorithm(ActionEvent actionEvent) {

        if (backgroundWorker != null) {
            backgroundWorker.stop();
            backgroundWorker = null;
        }

        clearDiagram();

        labelInfoBar.setText("");
        disableParameterControls(false);
    }

    private void disableParameterControls(boolean disable) {
        btnStartAlgorithm.setDisable(disable);
        btnFileChooser.setDisable(disable);
        toggleBtnViewSteps.setDisable(disable);
        comboBoxMetrics.setDisable(disable);
        comboBoxStartVector.setDisable(disable);
        textFieldLoadFile.setDisable(disable);
        textFieldNumberOfClusters.setDisable(disable);
        textFieldMinimumIterations.setDisable(disable);
        textFieldMaximumCentroidMovement.setDisable(disable);
        btnStopAlgorithm.setDisable(!disable);
        if (toggleBtnViewSteps.isSelected())
            btnNextStep.setDisable(!disable);
    }
}
