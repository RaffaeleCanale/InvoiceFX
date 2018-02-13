package com.wx.invoicefx.ui.views.archives.debug;

import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.interfaces.DataFile;
import com.wx.invoicefx.ui.views.settings.advanced.table.AbstractKeyValueTable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Enum<?>[] columns;

    @FXML
    private void initialize() {
        partitionPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            loadPartition(newValue.intValue());
        });

        partitionPagination.disableProperty().bind(singlePartitionToggle.selectedProperty().not());
        partitionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            load();
        });

        dataTable.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.DELETE)) {
                Set<Object[]> toRemove = dataTable.getSelectionModel().getSelectedItems().stream()
                        .collect(Collectors.toSet());

                int removed = 0;
                for (Object[] row : toRemove) {
                    try {
                        boolean success = cluster.removeFirst(r -> Arrays.deepEquals(r, row));
                        if (success) {
                            removed++;
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                System.out.println("removed = " + removed);
            }
        });
    }

    public void setCluster(ClusteredIndex cluster, Enum<?>[] columns) {
        this.cluster = cluster;
        this.columns = columns;

        partitionPagination.setPageCount(cluster.getStorage().getPartitionsCount());

        loadAllPartitions();
    }

    @FXML
    private void load() {
        if (singlePartitionToggle.isSelected()) {
            loadPartition(partitionPagination.getCurrentPageIndex());
        } else {
            loadAllPartitions();
        }
    }

    @FXML
    private void repartition() {
        try {
            cluster.repartition();
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPartition(int i) {
        DataFile partition = cluster.getStorage().getPartition(i);

        try {
            setData(partition.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAllPartitions() {
        try {
            setData(cluster.iterator().collect());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setData(List<Object[]> data) {
        dataTable.setItems(FXCollections.observableArrayList(data));

        if (!data.isEmpty()) {
            int colsCount = columns.length;

            if (dataTable.getColumns().size() != colsCount) {
                dataTable.getColumns().clear();

                for (int i = 0; i < colsCount; i++) {
                    TableColumn<Object[], Object> column = new TableColumn<>(columns[i].name());
                    final int index = i;

                    column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()[index]));

                    dataTable.getColumns().add(column);
                }
            }

            dataTable.getColumns().get(cluster.getSortKey()).setSortType(TableColumn.SortType.ASCENDING);
        }
    }
}
