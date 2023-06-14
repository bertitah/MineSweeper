package application;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BuscaminasApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        Label titleLabel = new Label("Buscaminas");
        titleLabel.setFont(Font.font("Arial", 24));
        root.getChildren().add(titleLabel);

        // Crear una transición de color multicolor
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(titleLabel.textFillProperty(), Color.RED)),
                new KeyFrame(Duration.seconds(1), new KeyValue(titleLabel.textFillProperty(), Color.GREEN)),
                new KeyFrame(Duration.seconds(2), new KeyValue(titleLabel.textFillProperty(), Color.BLUE))
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Repetir la transición indefinidamente
        timeline.setAutoReverse(true); // Invertir la dirección de la transición al final

        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();

        timeline.play(); // Iniciar la transición
    }

    public static void main(String[] args) {
        launch(args);
    }
}
