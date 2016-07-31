package app.tex;

import app.config.Config;
import app.config.preferences.SharedProperty;
import app.util.helpers.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by canale on 30.04.16.
 */
public class VatsSum {

    private final Map<Double, Double> sumPerVat;
    private final double[] vats;

    public VatsSum() {
        vats = Config.sharedPreferences().get(SharedProperty.VAT, Common::decodeDoubleArray);
        sumPerVat = new HashMap<>();

        for (double vat : vats) {
            sumPerVat.put(vat, 0.0);
        }
    }

    public void addFor(double vat, double sum) {
        if (!sumPerVat.containsKey(vat)) {
            throw new IllegalArgumentException("Unknown vat: " + vat);
        }

        sumPerVat.compute(vat, (k,v) -> v + sum);
    }

    public Stream<VatInfo> vatSums() {
        return IntStream.range(0, vats.length)
                .mapToObj(i -> {
                    double vat = vats[i];
                    double sum = sumPerVat.get(vat);

                    return new VatInfo(sum, vat, i);
                });
    }

    public Stream<VatInfo> nonZeroVatSums() {
        return vatSums().filter(v -> v.sum > 0.0);
    }

    public static class VatInfo {
        public final double sum;
        public final double vat;
        public final int vatIndex;
        public final double vatShare;

        public VatInfo(double sum, double vat, int vatIndex) {
            this.sum = sum;
            this.vat = vat;
            this.vatIndex = vatIndex;
            this.vatShare = Common.computeVatShare(vat, sum);
        }
    }

}
