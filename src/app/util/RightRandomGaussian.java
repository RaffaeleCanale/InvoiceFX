package app.util;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class RightRandomGaussian extends GaussianRandom {

    public RightRandomGaussian(long seed, double mean, double variance) {
        super(seed, mean, variance);
    }

    public RightRandomGaussian(long seed) {
        super(seed);
    }

    public RightRandomGaussian(double mean, double variance) {
        super(mean, variance);
    }

    public RightRandomGaussian() {
    }

    @Override
    protected double nextGaussian() {
        return Math.abs(super.nextGaussian());
    }
}
