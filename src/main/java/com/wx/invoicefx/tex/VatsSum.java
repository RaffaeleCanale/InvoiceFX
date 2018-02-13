package com.wx.invoicefx.tex;


import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.util.math.CurrencyUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.SystemColor.info;

/**
 * Created by canale on 30.04.16.
 */
public class VatsSum {

    private final Map<Vat, Double> sumPerVat;

    private VatsSum(Map<Vat, Double> sumPerVat) {
        this.sumPerVat = sumPerVat;
    }

    public VatsSum() {
        sumPerVat = new HashMap<>();
    }

    public void addFor(Vat vat, double sum) {
//        if (!sumPerVat.containsKey(vat)) {
//            throw new IllegalArgumentException("Unknown vat: " + vat);
//        }

        sumPerVat.putIfAbsent(vat, 0.0);
        sumPerVat.compute(vat, (k,v) -> v + sum);
    }

    public Stream<VatInfo> vatSums() {
        return sumPerVat.entrySet().stream()
                .map(e -> new VatInfo(e.getKey(), e.getValue()));
    }

    public Stream<VatInfo> nonZeroVatSums() {
        return vatSums().filter(v -> v.sum > 0.0);
    }

    public static class VatInfo {

        public final Vat vat;
        public final double sum;
        public final double vatShare;

        public VatInfo(Vat vat, double sum) {
            this.sum = sum;
            this.vat = vat;
            this.vatShare = CurrencyUtils.computeVatShare(vat.getValue(), sum);
        }
    }

    public VatsSum normalized() {
        Vat[] sorted = sumPerVat.keySet().stream()
                .sorted(Comparator.comparing(Vat::getCategory))
                .toArray(Vat[]::new);
        Map<Vat, Vat> normalisedMapping = new HashMap<>();
        for (int i = 0; i < sorted.length; i++) {
            Vat vat = sorted[i];
            Vat normalisedVat = new Vat(vat.getValue(), i);

            normalisedMapping.put(vat, normalisedVat);
        }



        Map<Vat, Double> newSumPerVat = new HashMap<>();
        sumPerVat.forEach((vat, sum) -> {
            newSumPerVat.put(normalisedMapping.get(vat), sum);
        });


        return new VatsSum(newSumPerVat);
    }

}
