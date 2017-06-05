package com.wx.invoicefx.view.archives;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * Utility class that creates a JavaFX component to visualize an invoice.
 * <p>
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class InvoiceViewer {

    /**
     * Create a component that displays the given invoice.
     *
     * @param invoice Invoice to display
     *
     * @return Component displaying that invoice
     */
    public static Node createViewer(Invoice invoice) {
        WebView view = new WebView();

        initViewer(view);
        setContent(view, invoice);

        return view;
    }

    /**
     * Initialize the given {@link WebView} for the display of invoices.
     *
     * @param view WebView component to initialize
     */
    public static void initViewer(WebView view) {
        view.setPrefWidth(200);
        view.setPrefHeight(400);
        view.setId("invoice_view");
        view.getEngine().setUserStyleSheetLocation(StageManager.getStyleSheet());
    }

    /**
     * Displays an invoice in the given {@link WebView}.
     * <p>
     * The {@link WebView} should have been initialized with {@link #initViewer(WebView)} first.
     *
     * @param view    WebView to display on
     * @param invoice Invoice to display
     */
    public static void setContent(WebView view, Invoice invoice) {
        view.getEngine().loadContent(InvoiceHtmlPrinter.print(invoice));
    }

    /**
     * Clears the display given {@link WebView}
     *
     * @param view WebView to clear
     */
    public static void clearContent(WebView view) {
        view.getEngine().loadContent("");
    }

}
