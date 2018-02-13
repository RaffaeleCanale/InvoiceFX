package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.update.UpdateHelper;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.util.concurrent.Callback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.pdfsam.ui.RingProgressIndicator;

import java.io.IOException;
import java.util.Optional;

import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.06.17.
 */
public class UpdateController {


    private enum State {
        LOADING("stage.settings.card.update.loading.status", null, null, true),
        UP_TO_DATE("stage.settings.card.update.up_to_date.status", "stage.settings.card.update.up_to_date.action", "right-button", false),
        HAS_UPDATE("stage.settings.card.update.has_update.status", "stage.settings.card.update.has_update.action", "custom-button", false),
        UPDATING("stage.settings.card.update.updating.status", null, null, true),
        FAILED("stage.settings.card.update.failed.status", "stage.settings.card.update.failed.action", "right-button", false);

        private final String labelKey;
        private final String buttonKey;
        private final String buttonStyleClass;
        private final boolean progressVisible;

        State(String labelKey, String buttonKey, String buttonStyleClass, boolean progressVisible) {
            this.labelKey = labelKey;
            this.buttonKey = buttonKey;
            this.buttonStyleClass = buttonStyleClass;
            this.progressVisible = progressVisible;
        }
    }

    @FXML
    private Button actionButton;
    @FXML
    private Label statusLabel;
    @FXML
    private RingProgressIndicator progressIndicator;

    private State state;
    private Throwable exception;

    @FXML
    private void initialize() {
        progressIndicator.getStylesheets().add(StageManager.getStyleSheet());

        loadUpdateState(false);
    }

    private void loadUpdateState(boolean delay) {
        setState(State.LOADING);
        executeAsync(UpdateHelper::getUpdateVersion, new Callback<Optional<Double>>() {
            @Override
            public Void success(Optional<Double> updateVersion) {
                if (delay) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        /* no-op */
                    }
                }

                Platform.runLater(() -> {
                    if (updateVersion.isPresent()) {
                        setState(State.HAS_UPDATE, updateVersion.get());
                    } else {
                        setState(State.UP_TO_DATE, UpdateHelper.getVersion());
                    }
                });

                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                Platform.runLater(() -> {
                    setState(State.FAILED);
                    exception = ex;
                });

                return null;
            }
        });
    }

    private void setState(State state, Object... statusParams) {
        exception = null;
        this.state = state;

        statusLabel.setText(Lang.getString(state.labelKey, statusParams));

        if (state.buttonKey == null) {
            actionButton.setVisible(false);
        } else {
            actionButton.setVisible(true);
            actionButton.setText(Lang.getString(state.buttonKey));
        }

        if (state.buttonStyleClass != null) {
            actionButton.getStyleClass().setAll("button", state.buttonStyleClass);
        }

        progressIndicator.setVisible(state.progressVisible);
        progressIndicator.setProgress(-1);
    }

    @FXML
    private void onAction() {
        switch (state) {
            case UPDATING:
            case LOADING:
                break;
            case UP_TO_DATE:
                loadUpdateState(true);
                break;
            case HAS_UPDATE:
                setState(State.UPDATING);
                executeAsync(() -> UpdateHelper.downloadUpdate(progressIndicator.progressProperty()),
                        new Callback<Object>() {
                            @Override
                            public Void success(Object o) {
                                try {
                                    UpdateHelper.executeUpdateScript();
                                } catch (IOException e) {
                                    failure(e);
                                }
                                return null;
                            }

                            @Override
                            public Void failure(Throwable ex) {
                                Platform.runLater(() -> {
                                    setState(State.FAILED);
                                    exception = ex;
                                });
                                return null;
                            }
                        });
                break;
            case FAILED:
                int choice = AlertBuilder.error(exception)
                        .key("stage.settings.card.update.see_error")
                        .button("stage.common.button.retry")
                        .button(ButtonType.CANCEL)
                        .show();

                if (choice == 0) {
                    loadUpdateState(false);
                }

                break;
        }

    }
}
