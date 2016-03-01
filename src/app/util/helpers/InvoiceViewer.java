package app.util.helpers;

import app.model.invoice.InvoiceModel;
import com.wx.fx.gui.window.StageManager;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class InvoiceViewer {

    public static Node createViewer(InvoiceModel invoice) {
        WebView view = new WebView();

        initViewer(view);
        setContent(view, invoice);

        return view;
    }

    public static void initViewer(WebView view) {
        view.setPrefWidth(200);
        view.setPrefHeight(400);
        view.setId("invoice_view");
        view.getEngine().setUserStyleSheetLocation(StageManager.getStyleSheet());
    }

    public static void setContent(WebView view, InvoiceModel invoice) {
        view.getEngine().loadContent(InvoiceHtmlPrinter.print(invoice));
    }

    public static void clearContent(WebView view) {
        view.getEngine().loadContent("");
    }

}
