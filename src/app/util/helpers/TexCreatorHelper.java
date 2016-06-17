package app.util.helpers;

import app.config.Config;
import app.config.preferences.properties.SharedProperty;
import app.legacy.model.invoice.InvoiceModel;
import app.tex.TexFileCreator;
import app.util.gui.AlertBuilder;
import com.wx.fx.Lang;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;

import java.io.IOException;

/**
 * Created on 16/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class TexCreatorHelper {

    /**
     * Launch the Tex compiler while using the label itself as a visual indicator of the process.
     *
     * @param invoice      Invoice to generate in LaTex
     * @param visualLabel Label used to visually indicated the progress of the process
     */
    public static void createAsync(InvoiceModel invoice, Labeled visualLabel) {
        String originalButtonText = visualLabel.getText();

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefWidth(visualLabel.getHeight() - 5);
        indicator.setPrefHeight(visualLabel.getHeight() - 5);

        visualLabel.setGraphic(indicator);
        visualLabel.setText(Lang.getString("overview.creating"));
        visualLabel.setMouseTransparent(true);
        visualLabel.setId("create_in_progress");

        Task<Void> creationTask = new Task<Void>() {

            @Override
            protected Void call() {
//                simulateTime();

                if (isCancelled()) {
                    return tearDown();
                }

                TexFileCreator texFileCreator = new TexFileCreator(invoice,
                        Config.sharedPreferences().getBooleanProperty(SharedProperty.SHOW_ITEM_COUNT));
                try {
                    String pdfName = texFileCreator.create();

                    invoice.setPdfFileName(pdfName);
                    Config.saveSafe(Config.invoicesManager());
                } catch (IOException e) {
                    AlertBuilder.error(e)
                            .key("errors.tex")
                            .show();
                }

                return tearDown();
            }

            private void simulateTime() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private Void tearDown() {
                Platform.runLater(() -> {
                    visualLabel.setGraphic(null);
                    visualLabel.setText(originalButtonText);
                    visualLabel.setId(null);
                    visualLabel.setMouseTransparent(false);
                });

                return null;
            }
        };
        new Thread(creationTask).start();
    }

}
