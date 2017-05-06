package app.gui.google;

import app.Stages;
import app.google.DriveConfigHelper;
import com.wx.fx.gui.window.StageManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class StatusPaneController {

    @FXML
    private Label label;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private ImageView imageView;

    public void initialize() {
        label.textProperty().bind(DriveConfigHelper.serviceStatusProperty());

        label.setOnMouseClicked(e -> StageManager.show(Stages.SETTINGS));
        DriveConfigHelper.currentStateProperty().addListener((observable, oldValue, newValue) -> {
            updateState(newValue);
        });

        updateState(DriveConfigHelper.currentStateProperty().getValue());
//        BooleanBinding serviceOnline = DriveConfigHelper.serviceOnlineBinding();
//
//        DriveConfigHelper.currentUserProperty().isNotEmpty().addListener((observable, oldValue, newValue) -> {
//            updateImage(serviceOnline.get());
//        });
//        serviceOnline.addListener((observable, oldValue, newValue) -> {
//            updateImage(newValue);
//        });
//
//        updateImage(serviceOnline.get());
//
//
//        progress.visibleProperty().bind(
//                DriveConfigHelper.serviceStatusProperty().isNotEmpty().and(DriveConfigHelper.serviceExceptionProperty().isNull())
//        );
//        imageView.visibleProperty().bind(progress.visibleProperty().not());
//
//        StringBinding status = Bindings.when(DriveConfigHelper.currentUserProperty().isNotEmpty())
//                .then(Bindings.when(DriveConfigHelper.serviceExceptionProperty().isNull())
//                        .then("google_connected").otherwise("google_failed")).otherwise("");
//        label.idProperty().bind(status);
    }

    private void updateState(DriveConfigHelper.State newValue) {
        progress.setVisible(false);
//        label.setOnMouseClicked(null);
        switch (newValue) {
            case CONNECTED:
                label.setId("status_google_connected");
                break;
            case ACTING:
                label.setId("status_google_acting");
                progress.setVisible(true);
                break;
            case FAILED:
//                label.setOnMouseClicked(e -> StageManager.show(Stages.SETTINGS));
                label.setId("status_google_failed");
                break;
            case FAILED_REGISTRATION:
//                label.setOnMouseClicked(e -> StageManager.show(Stages.SETTINGS));
                label.setId("status_google_failed");
                break;
            case UNREGISTERED:
                label.setId("");
                break;
            default:
                throw new AssertionError();
        }
    }

    private void updateImage(boolean isServiceOnline) {
        if (!DriveConfigHelper.isUserRegistered()) {
            setImage(null);

        } else if (isServiceOnline) {
            setImage("/icons/drive_connected.png");
        } else {
            setImage("/icons/drive_error.png");
        }

    }

    private void setImage(String path) {
        if (path == null) {
            imageView.setImage(null);
        } else {
            imageView.setImage(new Image(StatusPaneController.class.getResourceAsStream(path)));
        }
    }
}
