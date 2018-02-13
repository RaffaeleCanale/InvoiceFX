package com.wx.invoicefx.ui.views.settings;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.components.PagePane;
import com.wx.invoicefx.ui.components.RemoveableComponent;
import com.wx.invoicefx.ui.components.google.RemoteStatusPaneController;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.17.
 */
public class SettingsController implements StageController {

    @FXML
    private RemoteStatusPaneController remoteStatusPaneController;
    @FXML
    private PagePane pagePane;
    @FXML
    private ListView<String> tabListView;

    private final Set<Integer> disabledTabs = new HashSet<>();

    @Override
    public void setArguments(Object... args) {
        if (args.length > 0) {
            int page = (int) args[0];
            if (disabledTabs.contains(page)) {
                return;
            }

            tabListView.getSelectionModel().select(page);
        }
    }

    @FXML
    public void initialize() {
        tabListView.getItems().setAll(
                Lang.getString("stage.settings.tab.general"),
                Lang.getString("stage.settings.tab.rate"),
                Lang.getString("stage.settings.tab.document"),
                Lang.getString("stage.settings.tab.sync"),
                Lang.getString("stage.settings.tab.advanced")
        );
        if (!DriveManager.isInit()) {
            disableSyncRow();
        }

        pagePane.init();

        tabListView.getSelectionModel().select(0);
        pagePane.selectedPageProperty().bind(tabListView.getSelectionModel().selectedIndexProperty());
    }

    private void disableSyncRow() {
        final int syncRow = 3;

        disabledTabs.add(syncRow);
        tabListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty && item != null) {
                            setText(item);
                        }

                        if (getIndex() == syncRow) {
                            setDisable(true);
                        }
                    }
                };
            }
        });
    }

    @Override
    public void closing() {
        remoteStatusPaneController.onRemove();

        closing(pagePane.getChildren());
    }

    private void closing(Collection<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof RemoveableComponent) {
                ((RemoveableComponent) node).onRemove();
            } else if (node.getUserData() instanceof RemoveableComponent) {
                ((RemoveableComponent) node.getUserData()).onRemove();
            } else if (node instanceof Pane) {
                closing(((Pane) node).getChildren());
            }
        }
    }


    public void showAdvancedView() {
        ProgressIndicator progressIndicator = new ProgressIndicator(-1.0);
        progressIndicator.setMaxSize(64.0, 64.0);
        pagePane.getChildren().set(4, progressIndicator);

        ConcurrentUtil.executeAsync(() -> {
            FXMLLoader loader = new FXMLLoader(
                    SettingsController.class.getResource("/com/wx/invoicefx/ui/views/settings/advanced/AdvancedView.fxml"),
                    Lang.getBundle()
            );

            Pane viewer = loader.load();

            Platform.runLater(() -> {
                tabListView.setDisable(true);
                tabListView.setMaxWidth(tabListView.getWidth());
                Animator.instance().translateSlow(tabListView.maxWidthProperty(), 30.0).run();

                pagePane.getChildren().set(4, viewer);
            });
        }, ConcurrentUtil.NO_OP);


    }

    @FXML
    private void close() {
        StageManager.close(Stages.SETTINGS);
    }
}
