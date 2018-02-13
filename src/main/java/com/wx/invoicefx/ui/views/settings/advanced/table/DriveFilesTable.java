package com.wx.invoicefx.ui.views.settings.advanced.table;

import com.google.api.services.drive.model.File;
import com.wx.fx.Lang;
import com.wx.invoicefx.google.DriveManager;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wx.invoicefx.google.DriveManager.Action.REMOVE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public class DriveFilesTable extends BorderPane {



    private final TableView<File> tableView = new TableView<>();

    public DriveFilesTable() {
        init();
    }

    public void setItems(Collection<File> files) {
        tableView.getItems().setAll(files);
    }

    protected void init() {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<File, String> idColumn = new TableColumn<>(Lang.getString("stage.settings.advanced.table.drive_id"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<File, String> nameColumn = new TableColumn<>(Lang.getString("stage.settings.advanced.table.drive_name"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<File, Long> sizeColumn = new TableColumn<>(Lang.getString("stage.settings.advanced.table.drive_size"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        tableView.getColumns().addAll(idColumn, nameColumn, sizeColumn);


        tableView.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.DELETE)) {
                Set<File> toRemove = tableView.getSelectionModel().getSelectedItems().stream()
                        .filter(this::remove)
                        .collect(Collectors.toSet());

                tableView.getItems().removeAll(toRemove);
            }
        });

        setCenter(tableView);
    }


    private boolean remove(File file) {
        try {
            DriveManager.executeRemoveFile(file.getName());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
