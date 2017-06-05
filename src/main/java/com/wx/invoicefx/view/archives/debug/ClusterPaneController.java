package com.wx.invoicefx.view.archives.debug;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.interfaces.DataFile;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class ClusterPaneController {

    @FXML
    private RadioButton singlePartitionToggle;

    @FXML
    private RadioButton allPartitionsToggle;

    @FXML
    private ToggleGroup partitionGroup;
    @FXML
    private Pagination partitionPagination;
    @FXML
    private TableView<Object[]> dataTable;

    private ClusteredIndex cluster;

    public void initialize() {
        partitionPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            loadPartition(newValue.intValue());
        });

        partitionPagination.disableProperty().bind(singlePartitionToggle.selectedProperty().not());
        partitionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            load();
        });
    }

    public void setCluster(ClusteredIndex cluster) {
        this.cluster = cluster;

        partitionPagination.setPageCount(cluster.getStorage().getPartitionsCount());

        loadAllPartitions();
    }

    public void load() {
        if (singlePartitionToggle.isSelected()) {
            loadPartition(partitionPagination.getCurrentPageIndex());
        } else {
            loadAllPartitions();
        }
    }

    public void repartition() {
        try {
            cluster.repartition();
            load();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 13.05.17 Error handling
        }
    }

    private void loadPartition(int i) {
        DataFile partition = cluster.getStorage().getPartition(i);

        try {
            setData(partition.read());
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 13.05.17 Error handling
        }
    }

    private void loadAllPartitions() {
        try {
            setData(cluster.iterator().collect());
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 13.05.17 Error handling
        }
    }

    private void setData(List<Object[]> data) {
        dataTable.setItems(FXCollections.observableArrayList(data));

        if (!data.isEmpty()) {
            int colsCount = data.get(0).length;

            if (dataTable.getColumns().size() != colsCount) {
                dataTable.getColumns().clear();

                for (int i = 0; i < colsCount; i++) {
                    TableColumn<Object[], Object> column = new TableColumn<>("Col " + i);
                    final int index = i;

                    column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()[index]));

                    dataTable.getColumns().add(column);
                }
            }

            dataTable.getColumns().get(cluster.getSortKey()).setSortType(TableColumn.SortType.ASCENDING);
        }
    }

}
