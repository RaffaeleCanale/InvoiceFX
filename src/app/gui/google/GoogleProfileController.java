package app.gui.google;

import app.App;
import app.Stages;
import app.config.Config;
import app.google.DriveConfigHelper;
import app.model.invoice.InvoiceModel;
import app.model.item.ItemModel;
import app.util.ExceptionLogger;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.util.callback.SimpleCallback;
import com.wx.util.log.LogHelper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static app.google.DriveConfigHelper.Action.AUTHORIZE;
import static app.google.DriveConfigHelper.Action.GET_INFO;

/**
 * Created on 14/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class GoogleProfileController {

    private static final Logger LOG = LogHelper.getLogger(GoogleProfileController.class);
    private Image defaultImage;

    private enum ButtonAction {
        CONNECT("google_profile.add"),
        DISCONNECT("google_profile.remove"),
        CANCEL("google_profile.cancel");

        private final String text;

        ButtonAction(String key) {
            this.text = App.getLang().getString(key);
        }
    }

    @FXML
    private ImageView imageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Button button;
    @FXML
    private Button errorButton;

    private ButtonAction currentAction;
    private Thread authorizeThread;

    public void initialize() {
        Circle clip = new Circle(64, 64, 64);
        imageView.setClip(clip);

        ReadOnlyObjectProperty<DriveConfigHelper.State> state = DriveConfigHelper.currentStateProperty();
        state.addListener(new ChangeListener<DriveConfigHelper.State>() {

            private DriveConfigHelper.State lastPertinentState = state.get();

            @Override
            public void changed(ObservableValue<? extends DriveConfigHelper.State> observable, DriveConfigHelper.State oldValue, DriveConfigHelper.State newValue) {
                if (newValue != DriveConfigHelper.State.ACTING && newValue != lastPertinentState) {
                    load(newValue);
                    lastPertinentState = newValue;
                }
            }
        });
        load(state.get());
    }

    private void setDefaultImage() {
        if (defaultImage == null) {
            defaultImage = new Image(GoogleProfileController.class.getResourceAsStream("/icons/user-default.png"));
        }

        imageView.setImage(defaultImage);
    }

    private void load(DriveConfigHelper.State state) {
        switch (state) {
            case UNREGISTERED:
                nameLabel.setText("");
                setDefaultImage();
                imageView.setOnMouseClicked(event -> connectProfile());
                errorButton.setVisible(false);
                setButtonAction(ButtonAction.CONNECT);
                break;
            case ACTING:
                break;
            case FAILED_REGISTRATION:
                nameLabel.setText(DriveConfigHelper.currentUserProperty().get());
                setDefaultImage();
                imageView.setOnMouseClicked(event -> connectProfile());
                errorButton.setVisible(false);
                setButtonAction(ButtonAction.CONNECT);
                break;
            case FAILED:
                nameLabel.setText(DriveConfigHelper.currentUserProperty().get());
                if (imageView.getImage() == null) {
                    setDefaultImage();
                }
                imageView.setOnMouseClicked(null);
                errorButton.setVisible(true);
                setButtonAction(ButtonAction.DISCONNECT);
                break;
            case CONNECTED:
                DriveConfigHelper.performAction(GET_INFO, params -> loadUserInfo((Userinfoplus) params[0]));
                break;
        }
    }

    private void loadUserInfo(Userinfoplus userInfo) {
        String name = userInfo.getName();
        String imageUrl = userInfo.getPicture();
        String profileUrl = userInfo.getLink();

        if (name == null) {
            name = "user";
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

        imageView.setOnMouseClicked(profileUrl == null ? null : event -> App.openUrl(profileUrl));

        errorButton.setVisible(false);
        setButtonAction(ButtonAction.DISCONNECT);
    }

    public void viewError() {
        DriveConfigHelper.showExceptionAlert();
    }

    public void buttonAction() {
        switch (currentAction) {
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
        authorizeThread = DriveConfigHelper.performAction(AUTHORIZE, new SimpleCallback() {
            @Override
            public void success(Object[] params) {
                authorizeThread = null;

                LOG.info("Authorization successful");
                ArrayList<InvoiceModel> invoicesBackup = new ArrayList<>(Config.invoicesManager().get());
                ArrayList<ItemModel> itemsBackup = new ArrayList<>(Config.itemsManager().get());

                if (App.loadConfigSafe()) {
                    LOG.info("Merging managers");
                    if (addTo(Config.invoicesManager().get(), invoicesBackup, InvoiceModel::getId)) {
                        Config.saveSafe(Config.invoicesManager());
                    }
                    if (addTo(Config.itemsManager().get(), itemsBackup, ItemModel::getItemName)) {
                        Config.saveSafe(Config.itemsManager());
                    }

                    StageManager.close(Stages.SETTINGS);
                    StageManager.show(Stages.SETTINGS);
                } else {
                    LOG.info("Resetting old managers");
                    Config.invoicesManager().get().setAll(invoicesBackup);
                    Config.itemsManager().get().setAll(itemsBackup);

                }
            }
            @Override
            public void failure(Throwable ex) {
                LOG.warning("Authorization failed");
                authorizeThread = null;
            }

            @Override
            public void cancelled() {
                LOG.warning("Authorization cancelled");
            }
        });
        setButtonAction(ButtonAction.CANCEL);
    }


    private <E, F> boolean addTo(List<E> source, List<E> toAdd, Function<E, F> getId) {
        Map<F, E> idsMap = source.stream().collect(Collectors.toMap(getId, Function.identity()));

        boolean changed = false;

        for (E e : toAdd) {
            E existing = idsMap.get(getId.apply(e));
            if (existing == null) {
                source.add(e);
                changed = true;
            }
        }

        return changed;
    }

    private void disconnectProfile() {
        DriveConfigHelper.removeCurrentUser();
    }

    private void setButtonAction(ButtonAction action) {
        this.currentAction = action;
        this.button.setText(action.text);
    }
}
