package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.ui.components.settings.ExpandPane;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.io.file.FileUtil;
import com.wx.util.concurrent.ConcurrentUtil;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;
import static com.wx.invoicefx.config.Places.Dirs.TEX_TEMPLATE_ARCHIVES;
import static com.wx.invoicefx.config.preferences.shared.SharedProperty.TEX_TEMPLATE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.06.17.
 */
public class TemplateChooserController {

    @FXML
    private RadioButton defaultRadioButton;
    @FXML
    private RadioButton customRadioButton;
    @FXML
    private ExpandPane expandPane;
    @FXML
    private ToggleGroup toggleGroup;

    private String currentCustomName;

    @FXML
    private void initialize() {
        String templateFileName = AppResources.sharedPreferences().getString(TEX_TEMPLATE);

        if (templateFileName.isEmpty()) {
            selectToggle(defaultRadioButton);

        } else {
            customRadioButton.setText(templateFileName);
            currentCustomName = templateFileName;
            selectToggle(customRadioButton);
        }
    }


    @FXML
    private void selectCustom() {
        if (toggleGroup.getSelectedToggle().equals(customRadioButton)) {
            return;
        }

        if (currentCustomName != null) {
            File templateFile = Places.getCustomFile(TEX_TEMPLATE_ARCHIVES, currentCustomName);
            if (templateFile.isFile()) {
                setCustomTemplate(templateFile);

            } else {
                AlertBuilder.error()
                        .key("stage.settings.card.tex_template.errors.template_not_found", templateFile.getAbsolutePath())
                        .show();
            }

        } else {
            changeCustom();
        }
    }

    @FXML
    private void selectDefault() {
        if (toggleGroup.getSelectedToggle().equals(defaultRadioButton)) {
            return;
        }

        archiveCurrentTemplate();

        ConcurrentUtil.executeAsync(AppResources::getTexTemplate, ConcurrentUtil.NO_OP);
        AppResources.sharedPreferences().setProperty(TEX_TEMPLATE, "");

        selectToggle(defaultRadioButton);
    }

    @FXML
    private void changeCustom() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Places.getDir(TEX_TEMPLATE_ARCHIVES));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("LaTex", ".tex"));

        File selected = fileChooser.showOpenDialog(null);
        if (selected == null || !selected.isFile()) {
            return;
        }

        setCustomTemplate(selected);
    }

    private void setCustomTemplate(File file) {
        try {
            archiveCurrentTemplate();

            File newTemplate = Places.getCustomFile(DATA_DIR, file.getName());
            FileUtil.copyFile(file, newTemplate);

            AppResources.sharedPreferences().setProperty(TEX_TEMPLATE, newTemplate.getName());
            currentCustomName = newTemplate.getName();

            customRadioButton.setText(newTemplate.getName());
            selectToggle(customRadioButton);

        } catch (IOException e) {
            ExceptionLogger.logException(e);
            AlertBuilder.error(e)
                    .key("stage.settings.card.tex_template.errors.copy_failed")
                    .show();
        }
    }

    private void archiveCurrentTemplate() {
        try {
            File currentTemplate = AppResources.getTexTemplate();

            if (currentTemplate.exists()) {
                File archivedTemplate = Places.getCustomFile(TEX_TEMPLATE_ARCHIVES, currentTemplate.getName());
                currentTemplate.renameTo(archivedTemplate);
            }
        } catch (IOException e) {
            ExceptionLogger.logException(e);
        }
    }

    private void selectToggle(RadioButton toggle) {
        toggleGroup.selectToggle(toggle);
        expandPane.setSubLabel(toggle.getText());
    }
}
