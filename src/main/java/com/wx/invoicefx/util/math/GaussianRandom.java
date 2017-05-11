package com.wx.invoicefx.util.math;

import java.util.Random;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class GaussianRandom {

    private final Random random;
    protected final double mean;
    private final double variance;

    public GaussianRandom(long seed, double mean, double variance) {
        this.random = new Random(seed);
        this.mean = mean;
        this.variance = variance;
    }

    public GaussianRandom(long seed) {
        this(seed, 0, 1);
    }

    public GaussianRandom(double mean, double variance) {
        this.random = new Random();
        this.mean = mean;
        this.variance = variance;
    }


    public GaussianRandom() {
        this(0, 1);
    }

    public double next() {
        return mean + nextGaussian() * variance;
    }

    public int nextInt() {
        return (int) Math.round(next());
    }

    public long nextLong() {
        return Math.round(next());
    }

    protected double nextGaussian() {
        return random.nextGaussian();
    }
}
