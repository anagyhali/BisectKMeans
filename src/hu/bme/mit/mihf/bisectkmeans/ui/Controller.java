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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;

public class Controller {
    public TextField textFieldLoadFile;
    public TextField textFieldNumberOfClusters;
    public ToggleButton toggleBtnViewSteps;
    public TextField textFieldStopStatement;
    public ComboBox comboBoxMetrics;
    public ComboBox comboBoxStartVector;
    public GridPane rootGridPane;
    public Button btnStartAlgorithm;
    public Button btnNextStep;
    public Button btnStopAlgorithm;
    public Button btnFileChooser;
    public Label labelInfoBar;
    public Canvas canvasDiagram;

    private Thread backgroundWorker;

    public void handleWindowShownEvent() {
        textFieldLoadFile.setText(new File("data/default_input.txt").getAbsolutePath());

        comboBoxStartVector.getItems().setAll(StartVectorOptions.values());
        comboBoxStartVector.setValue(comboBoxStartVector.getItems().get(0));

        comboBoxMetrics.getItems().setAll(MetricsOptions.values());
        comboBoxMetrics.setValue(comboBoxMetrics.getItems().get(0));

        showEmptyDiagram();
    }

    public void startAlgorithm(ActionEvent actionEvent) {
        disableParameterControls(true);
        btnNextStep.setDisable(true);

        showEmptyDiagram();

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

        //TODO stop statement

        backgroundWorker = new Algorithm(
                (MetricsOptions)comboBoxMetrics.getValue(),
                (StartVectorOptions)comboBoxStartVector.getValue(),
                numberOfClusters,
                textFieldStopStatement.getText(),
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

    private void showEmptyDiagram() {
        //placeholder
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0,0,canvasDiagram.getWidth(),canvasDiagram.getHeight());
    }

    private void showInitialState(DataModel model) {
        //placeholder
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.TEAL);
        gc.fillRect(0,0,canvasDiagram.getWidth(),canvasDiagram.getHeight());
    }

    private void showResult(ArrayList<DataModel> result) {
        //placeholder
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillRect(0,0,canvasDiagram.getWidth(),canvasDiagram.getHeight());
    }

    private void showPartialResult(ArrayList<DataModel> partialResult) {
        //placeholder
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setFill(new Color(1, 1, 0, partialResult.size() / (double)Integer.parseInt(textFieldNumberOfClusters.getText())));
        gc.fillRect(0,0,canvasDiagram.getWidth(),canvasDiagram.getHeight());
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

        showEmptyDiagram();

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
        textFieldStopStatement.setDisable(disable);
        btnStopAlgorithm.setDisable(!disable);
        if (toggleBtnViewSteps.isSelected())
            btnNextStep.setDisable(!disable);
    }
}
