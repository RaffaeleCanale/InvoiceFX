package com.wx.invoicefx.util;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.util.string.SentenceItemsParser;
import com.wx.util.pair.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 03.08.17.
 */
public class ModelUtil {

    public static Pair<List<Client>, List<String>> parseClients(String sentence) {
        if (sentence.isEmpty()) {
            return Pair.of(Collections.emptyList(), Collections.emptyList());
        }

        Pair<List<String>, List<String>> parsed = SentenceItemsParser.splitStopWords(
                SentenceItemsParser.parseItems(sentence)
        );

        List<Client> clients = parsed.get1().stream()
                .map(clientName -> {
                    Client client = new Client();
                    client.setName(clientName);

                    return client;
                })
                .collect(Collectors.toList());

        return Pair.of(clients, parsed.get2());
    }

    private ModelUtil() {}
}
