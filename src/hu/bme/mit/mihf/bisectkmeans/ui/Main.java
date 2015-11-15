package hu.bme.mit.mihf.bisectkmeans.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Bisect k-means");
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Controller.class.getResourceAsStream("mainscene.fxml"));
        final Controller controller = loader.getController();
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent window)
            {
                controller.handleWindowShownEvent();
            }
        });
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
