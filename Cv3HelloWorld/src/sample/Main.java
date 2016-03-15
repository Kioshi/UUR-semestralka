package semestralka;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Button hello01BT = new Button();
        hello01BT.setText("Say 'Hello World'");

        Button hello02BT = new Button("Say lambda 'Hello world'");

        FlowPane root = new FlowPane();
        root.getChildren().add(hello01BT);
        root.getChildren().add(hello02BT);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
