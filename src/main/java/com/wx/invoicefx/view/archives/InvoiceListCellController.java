package com.wx.invoicefx.view.archives;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class InvoiceListCellController {

    @FXML
    private Label idLabel;
    @FXML
    private FlowPane clientsPane;
    @FXML
    private Label dateLabel;
    @FXML
    private Label sumLabel;


    public void setInvoice(Invoice invoice, Set<Long> markedClients) {
        idLabel.setText(String.valueOf(invoice.getId()));
        dateLabel.setText(invoice.getDate().toString());
        sumLabel.setText(String.valueOf(invoice.getSum()));

//        List<Client> clients = invoice.getPurchases().stream()
//                .map(Purchase::getClient)
//                .collect(Collectors.toMap(Client::getId, Function.identity(), (c1, c2) -> c1))
//                .values().stream()
//                .sorted(Comparator.comparing(Client::getName))
//                .collect(Collectors.toList());
        List<Client> clients = invoice.getPurchaseGroups().stream()
                .flatMap(purchaseGroup -> purchaseGroup.getClients().stream())
                .collect(Collectors.toMap(Client::getId, Function.identity(), (c1, c2) -> c1))
                .values().stream()
                .sorted(Comparator.comparing(Client::getName))
                .collect(Collectors.toList());


        for (Client client : clients) {
            Label label = new Label(client.getName());
            label.setPadding(new Insets(5));

            if (markedClients.contains(client.getId())) {
                label.getStyleClass().add("autocomplete-item-alt");
            } else {
                label.getStyleClass().add("autocomplete-item");
            }

            clientsPane.getChildren().add(label);
        }
    }



}
