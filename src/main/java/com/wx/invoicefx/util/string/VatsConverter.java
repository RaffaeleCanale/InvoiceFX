package com.wx.invoicefx.util.string;

import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.util.representables.TypeCaster;
import com.wx.util.representables.string.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.12.17.
 */
public class VatsConverter {

    public static String toString(Vats vats) {
        return getCaster().castIn(new ArrayList<>(vats.values()));
    }

    public static Vats toVats(String value) {
        return new Vats(getCaster().castOut(value).stream().toArray(Vat[]::new));
    }


    private static ListRepr<Vat> getCaster() {
        TypeCaster<String, Vat> vatCaster = new PairRepr<>(new DoubleRepr(), new IntRepr(), ",")
                .morph(Vat::new, Vat::getValue, Vat::getCategory);
        return new ListRepr<>(vatCaster, "/");
    }

}
