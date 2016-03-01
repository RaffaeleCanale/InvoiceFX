package app.util.helpers;

import app.App;
import app.config.Config;
import app.config.preferences.properties.SharedProperty;
import app.model.invoice.InvoiceModel;
import app.tex.TexFileCreator;
import app.util.gui.AlertBuilder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import java.io.IOException;

/**
 * Created on 16/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class TexCreatorHelper {

    public static void createAsync(InvoiceModel invoice, Button createButton) {
        String oldText = createButton.getText();

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefWidth(createButton.getHeight() - 5);
        indicator.setPrefHeight(createButton.getHeight() - 5);
        createButton.setGraphic(indicator);
        createButton.setText(App.getLang().getString("overview.creating"));
        createButton.setMouseTransparent(true);
        createButton.setId("create_in_progress");

        Task<Void> creationTask = new Task<Void>() {

            @Override
            protected void failed() {
                getException().printStackTrace();
            }

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
                    App.saveSafe(Config.invoicesManager());
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
                    createButton.setGraphic(null);
                    createButton.setText(oldText);
                    createButton.setId(null);
                    createButton.setMouseTransparent(false);
                });

                return null;
            }
        };
        new Thread(creationTask).start();
    }

}
