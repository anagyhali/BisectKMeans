package hu.bme.mit.mihf.bisectkmeans.ui;

import hu.bme.mit.mihf.bisectkmeans.model.DataModel;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;

/**
 * Created by dave on 2015. 12. 12..
 */
public class Diagramm {

    int dim;
    Canvas canvasDiagram;
    double max[];
    Color cols[];

    Diagramm(Canvas c,int dimension){
        dim=dimension;
        canvasDiagram = c;
        max =  new double[dimension];
        Color tmp[] = {Color.DARKMAGENTA, Color.BLUE, Color.ORANGE, Color.OLIVE, Color.AQUA, Color.BROWN,
                Color.PURPLE, Color.CHOCOLATE, Color.YELLOWGREEN, Color.GRAY, Color.DARKKHAKI, Color.LIGHTSALMON, Color.LIGHTCORAL,
                Color.LAWNGREEN};
        cols = tmp;
    }

    void drawDiagramm(ArrayList<DataModel> datas){
        int i=0;
        for(DataModel data : datas){
            if(i==0) {
                drawDiagramm(data, cols[i], true);
            }
            else{
                drawDiagramm(data,cols[i],false);
            }
            i++;
            if(i>12){
                i=0;//COLOR TÖMB MIATT
            }


        }



    }



    void drawDiagramm(DataModel datas,Color klasztercolor,boolean firstklaszter){
        GraphicsContext gc = canvasDiagram.getGraphicsContext2D();
        gc.setStroke(javafx.scene.paint.Color.RED);
        int x1=10;
        int y1=10;
        double x2=canvasDiagram.getWidth()-30;
        double y2 =canvasDiagram.getHeight()-30;
        gc.strokeRect(x1,y1,x2-10,y2-10);
        //FOK

        for(int i=0;i<dim;i++){
            max[i]=0;
        }

        for(int i=0; i < dim; i++){
            for (DataModel.GraphInfo trial: datas) {
                max[i] = getMax(trial.numberOfVertices[i],max[i]);
            }

        }

        gc.setStroke(Color.BLACK);
        gc.strokeText("[ max ]",x1 , y1 );
        gc.strokeText("[ 0 ]", x1, y2 );
        gc.strokeText("[ dim ]", x1, y2+15 );

        if(firstklaszter) {

            for (int i = 0; i < dim; i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                gc.setStroke(Color.BLACK);
                gc.setFill(Color.BLACK);
                double x = (width / (dim + 1)) * (i + 1) + 20;
                gc.strokeLine(x, y2, x, y1);
                gc.strokeText("[ " + i + " ]", x, y2 + 15);
                gc.strokeText("[ " + max[i] + " ]", x, y1);
            }
        }

        for (DataModel.GraphInfo trial: datas) {
            for (int i = 0; i < dim; i++) {
                double width = x2 - x1 - 20;
                double height = y2 - y1;
                double x = (width / (dim + 1)) * (i + 1) + 20;
                double y = height - (height / max[i] * trial.numberOfVertices[i]) + 10;
                gc.setStroke(klasztercolor);
                gc.setFill(klasztercolor);
                gc.fillRect(x, y, 5, 5);
                if (i != dim - 1) {
                    gc.strokeLine(x, y, (width / (dim + 1)) * (i + 2) + 20, height - (height / max[i+1] * trial.numberOfVertices[i + 1]) + 10);
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

    double getMax(double d, double d2){
        return d2 > d ? d2 : d;
    }

}
