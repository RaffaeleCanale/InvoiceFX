package com.wx.invoicefx.ui.components.google;


import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.fx.Lang;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.util.concurrent.Callback;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.logging.Logger;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.ENABLE_SYNC;
import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;

/**
 * Created on 14/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class GoogleProfileController {

    private static final Logger LOG = LogHelper.getLogger(GoogleProfileController.class);


    private enum ButtonAction {
        PROCESSING("google.button.processing", "button-progress"),
        CONNECT("google.button.connect", ""),
        DISCONNECT("google.button.disconnect", "error_button"),
        CANCEL("google.button.cancel", "");

        private final String text;
        private final String styleClass;

        ButtonAction(String key, String styleClass) {
            this.text = Lang.getString(key);
            this.styleClass = styleClass;
        }
    }

    @FXML
    private ImageView imageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Button mainButton;
    @FXML
    private Button errorButton;

    private ButtonAction currentAction;
    private Thread authorizeThread;
    private Image defaultImage;

    private Throwable serviceException;


    @FXML
    private void initialize() {
        if (!DriveManager.isInit()) {
            return;
        }

        if (DriveManager.isUserRegistered()) {
            loadUserInfo();

        } else {
            nameLabel.setText("");
            setDefaultImage();
            errorButton.setVisible(false);
            setButtonAction(ButtonAction.CONNECT);
        }
    }

    private void loadUserInfo() {
        setButtonAction(ButtonAction.PROCESSING);

        executeAsync(DriveManager::executeGetUserInfo, new Callback<Userinfoplus>() {
            @Override
            public Void success(Userinfoplus userInfo) {
                Platform.runLater(() -> {
                    String name = userInfo.getName();
                    String imageUrl = userInfo.getPicture();
                    String profileUrl = userInfo.getLink();

                    if (name == null) {
                        name = "user not found";
                        LOG.severe("No user found!");
                    }
                    nameLabel.setText(name);

                    if (imageUrl != null) {
                        try {
                            imageView.setImage(new Image(imageUrl));
                        } catch (Exception e) {
                            ExceptionLogger.logException(e);
                            LOG.warning("Image not set: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                            setDefaultImage();
                        }
                    }

                    errorButton.setVisible(false);
                    setButtonAction(ButtonAction.DISCONNECT);
                });
                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                Platform.runLater(() -> {
                    showErrorButton(ex);
                    setButtonAction(ButtonAction.DISCONNECT);
                });
                return null;
            }
        });
    }

    public void showErrorButton(Throwable ex) {
        nameLabel.setText(DriveManager.getCurrentUser());
        if (imageView.getImage() == null) {
            setDefaultImage();
        }
        errorButton.setVisible(true);
        setButtonAction(ButtonAction.DISCONNECT);

        serviceException = ex;
    }

    private void setButtonAction(ButtonAction action) {
        this.currentAction = action;
        this.mainButton.setText(action.text);
        this.mainButton.getStyleClass().setAll("button", action.styleClass);

        this.mainButton.setDisable(action == ButtonAction.PROCESSING);
    }

    private void setDefaultImage() {
        if (defaultImage == null) {
            defaultImage = new Image(GoogleProfileController.class.getResourceAsStream("/icons/user-default.png"));
        }

        imageView.setImage(defaultImage);
    }


    public void viewError() {
        showExceptionAlert();
    }

    public void buttonAction() {
        ButtonAction action = this.currentAction;

        setButtonAction(ButtonAction.PROCESSING);

        switch (action) {
            case CONNECT:
                connectProfile();
                break;
            case DISCONNECT:
                disconnectProfile();
                break;
            case CANCEL:
                if (authorizeThread != null) {
                    authorizeThread.interrupt();
                }
                break;
        }
    }

    private void connectProfile() {
        setButtonAction(ButtonAction.CANCEL);

        DriveManager.removeCurrentUser();
        executeAsync(DriveManager::executeAuthorize, new Callback<Object>() {
            @Override
            public Void success(Object o) {
                authorizeThread = null;

                LOG.info("Authorization successful");
                Platform.runLater(() -> {
                    initialize();
                    AppResources.localPreferences().booleanProperty(ENABLE_SYNC).set(true);
                });

                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                ex.printStackTrace();
                LOG.warning("Authorization failed");
                authorizeThread = null;

                Platform.runLater(() -> initialize());
                return null;
            }
        });
    }

    private void disconnectProfile() {
        DriveManager.removeCurrentUser();
        initialize();

        AppResources.localPreferences().booleanProperty(ENABLE_SYNC).set(false);
    }

    private void showExceptionAlert() {
        if (serviceException == null) {
            throw new IllegalArgumentException("There is no service exception");
        }

        int choice = AlertBuilder.error(serviceException)
                .key("google.error.service", serviceException.getMessage())
                .button("google.error.service.retry")
                .button("google.error.service.disconnect")
                .button(ButtonType.CANCEL)
                .show();

        if (choice == 0) {
            LOG.info("Attempting a reconnection");
            loadUserInfo();


        } else if (choice == 1) {
            DriveManager.removeCurrentUser();
            initialize();
        }
    }

}
