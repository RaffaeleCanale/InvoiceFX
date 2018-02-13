package com.wx.invoicefx.ui.views.archives.debug;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.model.save.table.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class DebugViewController implements StageController {


    @FXML
    private TabPane tabPane;

    @Override
    public void setArguments(Object... args) {
        InvoiceFxDataSet dataSet = (InvoiceFxDataSet) Objects.requireNonNull(args[0]);
        ModelSaver modelSaver = dataSet.getModelSaver();

        if (modelSaver == null) {
            return;
        }

        tabPane.getTabs().add(new Tab("Invoices", createClusterPane(
                modelSaver.getInvoicesTable(),
                InvoicesTable.Cols.values()
        )));
        tabPane.getTabs().add(new Tab("PurchaseGroups", createClusterPane(
                modelSaver.getPurchaseGroupsTable(),
                PurchaseGroupsTable.Cols.values()
        )));
        tabPane.getTabs().add(new Tab("ClientGroupsRelation", createClusterPane(
                modelSaver.getClientGroupsRelationTable(),
                ClientGroupsRelationTable.Cols.values()
        )));
        tabPane.getTabs().add(new Tab("Purchases", createClusterPane(
                modelSaver.getPurchasesTable(),
                PurchasesTable.Cols.values()
        )));
        tabPane.getTabs().add(new Tab("Items", createClusterPane(
                modelSaver.getItemsTable(),
                ItemsTable.Cols.values()
        )));
        tabPane.getTabs().add(new Tab("Clients", createClusterPane(
                modelSaver.getClientsTable(),
                ClientsTable.Cols.values()
        )));

    }

    private Pane createClusterPane(ClusteredIndex cluster, Enum<?>[] cols) {
        FXMLLoader loader = new FXMLLoader(
                DebugViewController.class.getResource("/com/wx/invoicefx/ui/views/archives/debug/ClusterPane.fxml"),
                Lang.getBundle());

        try {
            Pane clusterPane = loader.load();

            ClusterPaneController clusterPaneController = loader.getController();
            clusterPaneController.setCluster(cluster, cols);

            return clusterPane;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
