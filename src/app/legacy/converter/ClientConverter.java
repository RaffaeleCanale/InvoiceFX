package app.legacy.converter;

import app.model.client.Client;
import com.wx.util.representables.TypeCaster;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 25.06.16.
 */
public class ClientConverter implements TypeCaster<Client, String> {

    private final Client emptyClient;
    private final Map<String, Client> clientsBuffer = new HashMap<>();

    public ClientConverter() {
        emptyClient = new Client();
        emptyClient.setId(1);
        emptyClient.setName("");
    }

    @Override
    public Client castIn(String clientName) throws ClassCastException {
        if (clientName == null || clientName.isEmpty()) {
            return emptyClient;
        }

        Client client = clientsBuffer.get(clientName);
        if (client == null) {
            client = new Client();
            client.setName(clientName);
            client.setId(clientsBuffer.values().stream().mapToLong(Client::getId).max().orElse(1L) + 1);

            clientsBuffer.put(clientName, client);
        }

        return client;
    }

    @Override
    public String castOut(Client client) throws ClassCastException {
        return client.getName();
    }
}
