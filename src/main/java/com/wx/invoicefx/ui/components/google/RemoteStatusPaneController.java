package com.wx.invoicefx.ui.components.google;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.sync.SyncManager;
import com.wx.invoicefx.ui.components.RemoveableComponent;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.util.view.AlertBuilder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.pdfsam.ui.RingProgressIndicator;

import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;

/**
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class RemoteStatusPaneController implements RemoveableComponent {

    @FXML
    private Label syncLabel;
    @FXML
    private RingProgressIndicator syncProgress;

    private final ReadOnlyObjectProperty<SyncManager.StateContainer> syncState = AppResources.getSyncManager().currentStateProperty();


    private final ChangeListener<SyncManager.StateContainer> syncActionListener = (observable, oldValue, newValue) -> {
        updateState(newValue);
    };


    @FXML
    public void initialize() {
        syncProgress.getStylesheets().add(StageManager.getStyleSheet());
        syncLabel.setOnMouseClicked(this::onMouseClicked);

        updateState(syncState.get());
        syncState.addListener(syncActionListener);
    }

    @Override
    public void onRemove() {
        syncState.removeListener(syncActionListener);
    }

    private void showStateError() {
        AlertBuilder builder;
        if (syncState.get().getException() != null) {
            builder = AlertBuilder.error(syncState.get().getException());
        } else {
            builder = AlertBuilder.error();
        }

        int choice = builder.setHeader(Lang.getOptionalString("sync.status." + syncState.get().getState().name().toLowerCase()).orElse(""))
                .button(ButtonType.CANCEL)
                .button("stage.common.button.retry")
                .button("sync.button.open_settings")
                .show();

        if (choice == 1) {
            AppResources.getSyncManager().disableSync();
            AppResources.getSyncManager().enableSync();

            AppResources.triggerSync();
        } else if (choice == 2) {
            StageManager.show(Stages.SETTINGS, 3);
        }
    }

    private void onMouseClicked(Event e) {
        switch (syncState.get().getState()) {
            case PROCESSING_CONFLICT:
            case PROCESSING_OTHER:
            case PULL:
            case PUSH:
            case OFF:
            case PENDING:
            case UP_TO_DATE:
                break;
            case CONFLICTED:
                executeAsync(() -> {
                    AppResources.getSyncManager().enableSync();
                    AppResources.getSyncManager().synchronizeWithRemote(o -> null);
                }, null);
                break;
            case FAILED:
                showStateError();
            default:
                break;
        }
    }

    private void updateState(SyncManager.StateContainer state) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateState(state));
            return;
        }

        String stateName = state.getState().name().toLowerCase();
        syncLabel.setText(Lang.getOptionalString("sync.status." + stateName).orElse(""));
        syncLabel.getStyleClass().setAll("sync-state-default", "sync-state-" + stateName);


        syncProgress.progressProperty().unbind();
        if (hasProgress(state.getState())) {
            DoubleProperty progress = state.progressProperty();
            if (progress != null) {
                syncProgress.progressProperty().bind(progress.multiply(100.0));
            } else {
                syncProgress.progressProperty().setValue(-1);
            }
        } else {
            syncProgress.setProgress(0);
        }
    }

    private static boolean hasProgress(SyncManager.State state) {
        switch (state) {
            case PROCESSING_CONFLICT:
            case PROCESSING_OTHER:
            case PULL:
            case PUSH:
            case PENDING:
                return true;
            case CONFLICTED:
            case FAILED:
            case OFF:
            case UP_TO_DATE:
            default:
                return false;
        }
    }

}
