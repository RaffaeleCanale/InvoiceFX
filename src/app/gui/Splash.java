package app.gui;

import app.App;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Splash {


    private static final int SPLASH_SIZE = 300;
    private static final int SHADOW_RADIUS = 25;

    private final Pane splashLayout;
    private final Image icon;

    public Splash() {
        icon = new Image(Splash.class.getResourceAsStream("/icons/icon.png"));
        ImageView splash = new ImageView(icon);


        splashLayout = new BorderPane(splash);

        splashLayout.setEffect(new DropShadow(SHADOW_RADIUS, Color.BLACK));
    }

    public void showSplash(Stage stage) {
        Scene splashScene = new Scene(splashLayout);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(icon);
        stage.setTitle(App.APPLICATION_NAME);

        splashScene.setFill(Color.TRANSPARENT);
        stage.setScene(splashScene);
        stage.setWidth(SPLASH_SIZE + 2*SHADOW_RADIUS);
        stage.setHeight(SPLASH_SIZE + 2*SHADOW_RADIUS);
        stage.show();
    }
}