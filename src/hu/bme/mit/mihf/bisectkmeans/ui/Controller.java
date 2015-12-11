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
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setStroke(javafx.scene.paint.Color.RED);
        int x1=10;
        int y1=10;
        double x2=canvasDiagram.getWidth()-30;
        double y2 =canvasDiagram.getHeight()-30;
        gc.strokeRect(x1,y1,x2-10,y2-10);
        //FOK
        int n=model.get(0).numberOfVertices.length;

        for (DataModel.GraphInfo trial: model) {
            double max = getMax(trial.numberOfVertices);

            Color cols[] = {Color.DARKMAGENTA, Color.BLUE, Color.ORANGE, Color.OLIVE, Color.AQUA, Color.BROWN,
                    Color.PURPLE, Color.CHOCOLATE, Color.YELLOWGREEN, Color.GRAY, Color.DARKKHAKI, Color.LIGHTSALMON, Color.LIGHTCORAL,
                    Color.LAWNGREEN};
            for (int i = 0; i < n; i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                gc.setStroke(cols[i]);
                gc.setFill(cols[i]);
                double x = (width / (n + 1)) * (i + 1) + 20;
                double y = height - (height / max * trial.numberOfVertices[i]) + 10;
                gc.strokeLine(x, y2, x, y1);
            }

            for (int i = 0; i < n; i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                double x = (width / (n + 1)) * (i + 1) + 20;
                double y = height - (height / max * trial.numberOfVertices[i]) + 10;
                gc.setStroke(cols[i]);
                gc.setFill(cols[i]);
                gc.strokeText("[" + i + "]", x, y2 + 15);
                gc.fillRect(x, y, 5, 5);
                if (i != n - 1) {
                    gc.strokeLine(x, y, (width / (n + 1)) * (i + 2) + 20, height - (height / max * trial.numberOfVertices[i + 1]) + 10);
                }
            }
        }

    }

    public static int getMax(int[] inputArray){
        int maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }


    private void showResult(ArrayList<DataModel> result) {
        if (1 == 1)return;
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setStroke(javafx.scene.paint.Color.RED);
        int x1=10;
        int y1=10;
        double x2=canvasDiagram.getWidth()-30;
        double y2 =canvasDiagram.getHeight()-30;
        gc.strokeRect(x1,y1,x2-10,y2-10);
        //FOK
        int n=13;

        int trial[]= {1,2,3,4,4,4,5,5,56,4,67,8,54};
        int max = getMax(trial);

        Color cols[] ={Color.DARKMAGENTA,Color.BLUE,Color.ORANGE,Color.OLIVE,Color.AQUA,Color.BROWN,
                Color.PURPLE,Color.CHOCOLATE,Color.YELLOWGREEN,Color.GRAY,Color.DARKKHAKI,Color.LIGHTSALMON, Color.LIGHTCORAL,
                Color.LAWNGREEN};
        for(int i=0; i<n;i++) {
            double width = x2 - x1 - 20;
            double height = y2 - y1;
            gc.setStroke(cols[i]);
            gc.setFill(cols[i]);
            double x = (width / (n + 1)) * (i + 1) + 20;
            double y = height - (height / max * trial[i]) + 10;
            gc.strokeLine(x, y2, x, y1);
        }

        for(DataModel item : result){
            for(int i=0; i<n;i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                double x = (width / (n + 1)) * (i + 1) + 20;
                double y = height - (height / max * trial[i]) + 10;
                gc.setStroke(cols[i]);
                gc.setFill(cols[i]);
                gc.strokeText("["+i+"]",x,y2+15);
                gc.fillRect(x,y,5,5);
                if(i!=n-1){
                    gc.strokeLine(x,y,(width/(n+1))*(i+2)+20,height - (height/max*trial[i+1])+10);
                }
            }
        }

    }

    private void showPartialResult(ArrayList<DataModel> partialResult) {
        if (1 == 1)return;
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setStroke(javafx.scene.paint.Color.RED);
        int x1=10;
        int y1=10;
        double x2=canvasDiagram.getWidth()-30;
        double y2 =canvasDiagram.getHeight()-30;
        gc.strokeRect(x1,y1,x2-10,y2-10);
        //FOK
        int n=13;

        int trial[]= {1,2,3,4,4,4,5,5,56,4,67,8,54};
        int max = getMax(trial);

        Color cols[] ={Color.DARKMAGENTA,Color.BLUE,Color.ORANGE,Color.OLIVE,Color.AQUA,Color.BROWN,
                Color.PURPLE,Color.CHOCOLATE,Color.YELLOWGREEN,Color.GRAY,Color.DARKKHAKI,Color.LIGHTSALMON, Color.LIGHTCORAL,
                Color.LAWNGREEN};
        for(int i=0; i<n;i++) {
            double width = x2 - x1 - 20;
            double height = y2 - y1;
            gc.setStroke(cols[i]);
            gc.setFill(cols[i]);
            double x = (width / (n + 1)) * (i + 1) + 20;
            double y = height - (height / max * trial[i]) + 10;
            gc.strokeLine(x, y2, x, y1);
        }

        for(DataModel item : partialResult){
            for(int i=0; i<n;i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                double x = (width / (n + 1)) * (i + 1) + 20;
                double y = height - (height / max * trial[i]) + 10;
                gc.setStroke(cols[i]);
                gc.setFill(cols[i]);
                gc.strokeText("["+i+"]",x,y2+15);
                gc.fillRect(x,y,5,5);
                if(i!=n-1){
                    gc.strokeLine(x,y,(width/(n+1))*(i+2)+20,height - (height/max*trial[i+1])+10);
                }
            }
        }

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
