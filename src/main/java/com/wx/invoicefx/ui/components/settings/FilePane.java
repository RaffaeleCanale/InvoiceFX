package com.wx.invoicefx.ui.components.settings;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.fx.transfer.TransferController;
import com.wx.fx.transfer.TransferTask;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.io.file.FileUtil;
import com.wx.util.concurrent.Callback;
import javafx.beans.NamedArg;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static com.wx.fx.transfer.TransferTask.Action.MOVE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class FilePane extends BorderPane {

    private final Label pathLabel;

    private StringProperty pathProperty;

    private boolean offerToTransferFiles = false;
    private boolean isDirectory = false;
    private Collection<? extends FileChooser.ExtensionFilter> fileFilters = Collections.emptyList();

    public FilePane(@NamedArg("text") String text) {
        pathLabel = new Label();
        Label label = new Label(text);
        Button changeButton = new Button(Lang.getString("stage.settings.components.button.change"));
        VBox vBox = new VBox(10.0, label, pathLabel);


        pathLabel.getStyleClass().add("setting-value");
        changeButton.setPrefSize(75, 50);
        changeButton.setFocusTraversable(false);
        changeButton.getStyleClass().add("right-button");

        changeButton.setOnAction(this::handleChange);
        pathLabel.setOnMouseClicked(this::openPath);

        setCenter(vBox);
        setRight(changeButton);
    }

    public void bindWith(StringProperty property) {
        this.pathProperty = property;
        this.pathLabel.textProperty().bind(property);
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public void setOfferToTransferFiles(boolean offerToTransferFiles) {
        if (!isDirectory) {
            throw new IllegalArgumentException("This option is only available for directory properties");
        }
        this.offerToTransferFiles = offerToTransferFiles;
    }

    public void setFileFilters(Collection<? extends FileChooser.ExtensionFilter> fileFilters) {
        if (isDirectory) {
            throw new IllegalArgumentException("This option is only available for file properties");
        }
        this.fileFilters = fileFilters;
    }

    private void openPath(MouseEvent mouseEvent) {
        File dir = getCurrentValue();

        if (dir != null) {
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();

                if (dir == null || dir.exists()) {
                    return;
                }
            }

            DesktopUtils.open(dir);
        }
    }

    private File getCurrentValue() {
        if (pathProperty.get().isEmpty()) {
            return null;
        }

        File file = new File(pathProperty.get());
        if (!file.exists()) {
            return null;
        }

        return file;
    }

    private ChooserWrapper getChooser() {
        if (isDirectory) {
            return new DirChooserWrapper();
        } else {
            FileChooserWrapper wrapper = new FileChooserWrapper();
            wrapper.chooser.getExtensionFilters().setAll(fileFilters);
            return wrapper;
        }
    }

    private void handleChange(ActionEvent actionEvent) {
        ChooserWrapper chooser = getChooser();

        File currentDir = getCurrentValue();
        if (currentDir != null && currentDir.getParentFile() != null && currentDir.getParentFile().exists()) {
            chooser.setInitialDirectory(currentDir.getParentFile());
        }

        File result = chooser.showDialog(null);

        if (result == null || result.equals(currentDir)) {
            return;
        }

        if (isDirectory) {
            try {
                FileUtil.autoCreateDirectory(result);
            } catch (IOException e) {
                AlertBuilder.error(e)
                        .key("stage.settings.components.errors.file_not_found",
                                result.getAbsolutePath())
                        .show();
                return;
            }
        }

        if (currentDir != null && isDirectory && offerToTransferFiles) {
            File[] files = currentDir.listFiles();

            if (files != null && files.length > 0) {
                int choice = AlertBuilder.confirmation()
                        .key("stage.settings.card.invoices_dir.dialogs.migrate",
                                files.length)
                        .show();
                if (choice == 0) { // Migrate
                    moveFiles(files, result);
                }
            }
        }


        pathProperty.set(result.getAbsolutePath());
    }

    public static void moveFiles(File[] files, File newDirectory) {
        if (files.length == 0) {
            return;
        }

        // TODO: 17.06.17 Where to put this?
        StageManager.show(TransferController.STAGE_INFO,
                new TransferTask.Builder()
                        .action(MOVE, files, newDirectory)
                        .build(),
                new Callback<Object>() {
                    @Override
                    public Void success(Object args) {
                        return null;
                    }

                    @Override
                    public Void failure(Throwable ex) {
                        // TODO: 22.06.17 Error handling
                        AlertBuilder.error(ex)
                                .key("errors.copy_failed")
                                .show();
                        return null;
                    }
                });
    }

    private interface ChooserWrapper {
        void setInitialDirectory(File dir);

        File showDialog(Window ownerWindow);
    }

    private static class DirChooserWrapper implements ChooserWrapper {
        private final DirectoryChooser chooser = new DirectoryChooser();

        @Override
        public void setInitialDirectory(File dir) {
            chooser.setInitialDirectory(dir);
        }

        @Override
        public File showDialog(Window ownerWindow) {
            return chooser.showDialog(ownerWindow);
        }
    }

    private static class FileChooserWrapper implements ChooserWrapper {
        private final FileChooser chooser = new FileChooser();

        @Override
        public void setInitialDirectory(File dir) {
            chooser.setInitialDirectory(dir);
        }

        @Override
        public File showDialog(Window ownerWindow) {
            return chooser.showOpenDialog(ownerWindow);
        }
    }

}
