package com.wx.invoicefx.view.archives.debug;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.model.save.SaveManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class DebugViewController implements StageController {


    @FXML
    private TabPane tabPane;

    @Override
    public void setArguments(Object... args) {
        // TODO: 15.05.17 Get save manager from args
        SaveManager saveManager = new SaveManager(Places.getDir(DATA_DIR), null);

        try {
            tabPane.getTabs().add(new Tab("Invoices", createClusterPane(saveManager.getInvoicesTable())));
            tabPane.getTabs().add(new Tab("Purchases", createClusterPane(saveManager.getPurchasesTable())));
            tabPane.getTabs().add(new Tab("Items", createClusterPane(saveManager.getItemsTable())));
            tabPane.getTabs().add(new Tab("Clients", createClusterPane(saveManager.getClientsTable())));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pane createClusterPane(ClusteredIndex cluster) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                DebugViewController.class.getResource("/com/wx/invoicefx/view/archives/debug/ClusterPane.fxml"),
                Lang.getBundle());

        Pane clusterPane = loader.load();

        ClusterPaneController clusterPaneController = loader.getController();
        clusterPaneController.setCluster(cluster);


        return clusterPane;
    }

}
