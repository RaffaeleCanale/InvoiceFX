package com.wx.invoicefx.io.util.data;

import com.wx.invoicefx.util.math.GaussianRandom;
import com.wx.io.TextAccessor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class DummyData {

    private static final long SEED = 24;


    private static final GaussianRandom DATE = new GaussianRandom(SEED+4, LocalDate.now().toEpochDay(), 50);
    private static final Random RANDOM = new Random(SEED+5);


    private static final List<String> CLIENT_NAMES = loadTestResources("/Clients.txt");
    private static final List<String> ADDRESSES_NAMES = loadTestResources("/Addresses.txt");
    private static final List<String> ITEMS_NAMES = loadTestResources("/Items.txt");

    private static List<String> loadTestResources(String name) {
        try (TextAccessor accessor = new TextAccessor().setIn(DummyData.class.getResourceAsStream(name))) {
            List<String> result = new ArrayList<>();
            accessor.read(result);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDate generateDate() {
        return LocalDate.ofEpochDay(DATE.nextLong());
    }

    public static String generateClientName() {
        return CLIENT_NAMES.get(RANDOM.nextInt(CLIENT_NAMES.size()));
    }

    public static String generateAddressName() {
        return ADDRESSES_NAMES.get(RANDOM.nextInt(ADDRESSES_NAMES.size()));
    }

    public static String generateItemName() {
        return ITEMS_NAMES.get(RANDOM.nextInt(ITEMS_NAMES.size()));
    }


}
