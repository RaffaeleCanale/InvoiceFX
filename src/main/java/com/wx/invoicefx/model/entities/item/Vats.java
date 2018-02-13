package com.wx.invoicefx.model.entities.item;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.12.17.
 */
public class Vats {

    private final Map<Integer, Vat> vats;

    private Vats(Map<Integer, Vat> vats) {
        this.vats = vats;
    }

    public Vats(Vat[] vats) {
        this.vats = Stream.of(vats).collect(Collectors.toMap(
                Vat::getCategory,
                Function.identity()
        ));
    }


    public Optional<Vat> getVat(int category) {
        return Optional.ofNullable(vats.get(category));
    }

    public Collection<Vat> values() {
        return vats.values();
    }

    public Vats difference(Vats other) {
        Map<Integer, Vat> difference = new HashMap<>();

        difference.putAll(vats);
        difference.putAll(other.vats);

        Iterator<Integer> it = difference.keySet().iterator();
        while (it.hasNext()) {
            int category = it.next();

            if (Objects.equals(vats.get(category), other.vats.get(category))) {
                it.remove();
            }
        }

        return new Vats(difference);
    }

    public boolean contains(Vat vat) {
        Vat current = vats.get(vat.getCategory());

        return current != null && current.getValue() == vat.getValue();
    }

    public Vats normalize() {
        Vat[] result = vats.values().stream()
                .sorted(Comparator.comparing(Vat::getCategory))
                .toArray(Vat[]::new);

        for (int i = 0; i < result.length; i++) {
            result[i] = new Vat(result[i].getValue(), i);
        }

        return new Vats(result);
    }
}
