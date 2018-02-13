package com.wx.invoicefx.util;

import com.wx.invoicefx.Bootstrap;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.io.util.data.DummyModels;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.util.concurrent.Callback;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class SampleConfig extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Bootstrap.bootstrapWithoutUI((Callback) o -> {
            try {
                ModelSaver modelSaver = new ModelSaver(Places.getDir(DATA_DIR));
                List<Invoice> invoices = DummyModels.generateInvoices(200);

                modelSaver.addInvoices(invoices);

                System.out.println("Done");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }
}
