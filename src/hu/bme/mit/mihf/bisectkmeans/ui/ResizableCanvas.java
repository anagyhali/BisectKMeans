package hu.bme.mit.mihf.bisectkmeans.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.beans.PropertyChangeListener;

public class ResizableCanvas extends Canvas {

    public ResizableCanvas() {

    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    @Override
    public double minWidth(double height) {
        return 0.0;
    }

    @Override
    public double minHeight(double height) {
        return 0.0;
    }
}