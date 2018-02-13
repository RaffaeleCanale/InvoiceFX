package com.wx.invoicefx.ui.views.backup;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.backup.BackupManager;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.DisabledAnimator;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.ui.views.sync.DataSetBoxController;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.invoicefx.util.view.FormattedListFactory;
import com.wx.util.concurrent.Callback;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.Date;

import static com.wx.invoicefx.ui.views.sync.DataSetChooserController.DialogType.RESTORE_BACKUP;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 26.06.17.
 */
public class BackupController implements StageController {


    @FXML
    private ListView<DataSet> listView;
    @FXML
    private DataSetBoxController dataSetBoxController;

    @FXML
    private void initialize() {
        // LIST VIEW
        listView.setCellFactory(new FormattedListFactory<>(dataSet -> {
            Index index = dataSet.getIndex();

            double version = index.getVersion();
            Date lastModifiedDate = index.getLastModifiedDate();

            String formattedDate = InvoiceFormats.formatDateTime(lastModifiedDate);
            return Lang.getString("stage.backup.label.backup", version, formattedDate);
        }));


        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            VBox pane = dataSetBoxController.getContentPane();
            if (newValue.intValue() < 0) {
                Animator.instance().fadeOut(pane).run();
            } else {
                dataSetBoxController.setDataSet(listView.getItems().get(newValue.intValue()));

                double height = pane.getHeight();

                if (pane.getOpacity() == 0) {
                    Animator.instance().fadeIn(pane).run();
                } else if (newValue.intValue() > oldValue.intValue()) {
                    Animator.instance().translate(pane.layoutYProperty(), -height)
                            .then(Animator.instance().translate(pane.layoutYProperty(), height, 0))
                            .run();
                } else {
                    Animator.instance().translate(pane.layoutYProperty(), height)
                            .then(Animator.instance().translate(pane.layoutYProperty(), -height, 0))
                            .run();
                }

            }
        });

        // DATA SET BOX
        new DisabledAnimator().fadeOut(dataSetBoxController.getContentPane()).run();

        dataSetBoxController.setChooseButtonText(Lang.getString("stage.backup.button.recover"));
        dataSetBoxController.setOnChooseHandler(event -> {
            DataSet backup = dataSetBoxController.getDataSet();

            StageManager.show(Stages.DATA_SET_CHOOSER, RESTORE_BACKUP, new Callback<Object>() {
                @Override
                public Void success(Object o) {
                    Platform.runLater(() -> StageManager.close(Stages.BACK_UP));
                    return null;
                }

                @Override
                public Void failure(Throwable ex) {
                    AlertBuilder.error(ex)
                            .key("stage.backup.errors.restore_failed")
                            .show();
                    return null;
                }
            }, backup);
        });
    }

    @Override
    public void setArguments(Object... args) {
        ConcurrentUtil.executeAsync(BackupManager::getAllBackups, backups -> {
            Platform.runLater(() -> listView.setItems(FXCollections.observableArrayList(backups)));
            return null;
        });
    }


    @FXML
    private void onClose() {
        StageManager.close(Stages.BACK_UP);
    }


}
