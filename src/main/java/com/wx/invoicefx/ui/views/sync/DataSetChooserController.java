package com.wx.invoicefx.ui.views.sync;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.backup.BackupManager;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.LocalDataSet;
import com.wx.invoicefx.sync.PushPullSync;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.util.concurrent.LazyCallback;
import com.wx.util.concurrent.Callback;
import com.wx.util.concurrent.ConcurrentUtil;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.pdfsam.ui.RingProgressIndicator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 26.06.17.
 */
public class DataSetChooserController implements StageController {


    private static final Logger LOG = LogHelper.getLogger(DataSetChooserController.class);

    public enum DialogType {
        SOLVE_CONFLICT,
        SOLVE_CORRUPTED,
        RESTORE_BACKUP
    }


    @FXML
    private Label header;
    @FXML
    private Label subHeader;
    @FXML
    private HBox choicePane;
    @FXML
    private RingProgressIndicator progressIndicator;

    private Callback<Object> callback;

    @Override
    public void setArguments(Object... args) {
        DialogType type = (DialogType) args[0];
        callback = (Callback<Object>) args[1];

        header.setText(Lang.getString("stage.repository_chooser.label." + type.name().toLowerCase() + ".header"));
        subHeader.setText(Lang.getString("stage.repository_chooser.label." + type.name().toLowerCase() + ".sub_header"));

        switch (type) {
            case RESTORE_BACKUP:
                initRestoreBackup((DataSet) args[2]);
                break;
            case SOLVE_CONFLICT:
                initSolveConflict();
                break;
            case SOLVE_CORRUPTED:
                initSolveCorrupted();
                break;
            default:
                throw new AssertionError();
        }
    }

    private void initRestoreBackup(DataSet backUp) {
        DataSet localRepository = AppResources.getLocalDataSet();
        Label rightArrow = new Label();

        rightArrow.getStyleClass().setAll("repository-chooser-right-arrow");

        choicePane.getChildren().addAll(
                createChoiceBox(localRepository),
                rightArrow,
                createChoiceBox(backUp)
        );
    }

    private void initSolveConflict() {
        DataSet local = AppResources.getLocalDataSet();
        DataSet remote = AppResources.getSyncManager().getRemote().get();

        choicePane.getChildren().addAll(
                createChoiceBox(local),
                createChoiceBox(remote)
        );
    }

    private void initSolveCorrupted() {
        DataSet local = AppResources.getLocalDataSet();
        Optional<DataSet> remote = AppResources.getSyncManager().getRemote();

        choicePane.getChildren().add(createChoiceBox(local));
        if (remote.isPresent()) {
            choicePane.getChildren().add(createChoiceBox(remote.get()));
        }

        try {
            List<DataSet> allBackups = BackupManager.getAllBackups();
            if (!allBackups.isEmpty()) {
                choicePane.getChildren().add(createChoiceBox(allBackups));
            }
        } catch (IOException e) {
            /* no-op */
        }
    }

    private void showProgress() {
        choicePane.setVisible(false);
        progressIndicator.setVisible(true);
    }

    @Override
    public void closing() {
        if (callback != null) {
            callback.cancelled();
        }
    }

    @FXML
    private void onClose() {
        StageManager.close(Stages.DATA_SET_CHOOSER);
    }

    private Pane createChoiceBox(DataSet dataSet) {
        return createChoiceBox(Collections.singletonList(dataSet));
    }

    private Pane createChoiceBox(List<DataSet> dataSets) {
        FXMLLoader loader = new FXMLLoader(
                DataSetChooserController.class.getResource("/com/wx/invoicefx/ui/views/sync/DataSetBox.fxml"),
                Lang.getBundle()
        );

        Pane pane = load(loader);
        DataSetBoxController controller = loader.getController();

        controller.setDataSets(dataSets);
        controller.setOnChooseHandler(e -> {
            showProgress();

            Callback<Object> close = (LazyCallback<Object>) (err, res) -> {
                DataSetChooserController.this.callback = null;

                Platform.runLater(() -> StageManager.close(Stages.DATA_SET_CHOOSER));
                return null;
            };

            ConcurrentUtil.executeAsync(() -> choose(controller.getDataSet()), close.then(callback));
        });

        return pane;
    }

    private void choose(DataSet dataSet) throws IOException {
        if (dataSet.isCorrupted()) {
            throw new IllegalArgumentException("Cannot choose a corrupted data set");
        }

        LocalDataSet local = AppResources.getLocalDataSet();

        if (dataSet == local) {
            Optional<DataSet> remote = AppResources.getSyncManager().getRemote();

            if (!remote.isPresent()) {
                return; // Nothing to do
            }


            LOG.info("Force push local to remote");
            PushPullSync pushPullSync = new PushPullSync(local, remote.get());

            Platform.runLater(() -> {
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().bind(pushPullSync.progressProperty().multiply(100.0));
            });

            pushPullSync.pushForce();

        } else {


            LOG.info("Restoring data from " + dataSet.getIndex().getVersion() + " (" + dataSet.getProperty("type").orElse("???") + ")");

            PushPullSync pushPullSync = new PushPullSync(local, dataSet);

            Platform.runLater(() -> {
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().bind(pushPullSync.progressProperty().multiply(100.0));
            });


            double baseVersion = AppResources.getSyncManager().getRemote().map(r -> r.getIndex().getVersion())
                    .orElse(local.getIndex().getBaseVersion());

            pushPullSync.pullForce();

            local.getIndex().setVersion(baseVersion + Index.VERSION_INCREMENT);
            local.getIndex().setBaseVersion(baseVersion);
            local.getIndex().save();

        }


    }

    private static <T> T load(FXMLLoader loader) {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
