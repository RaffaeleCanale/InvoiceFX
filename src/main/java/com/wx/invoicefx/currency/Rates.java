package com.wx.invoicefx.currency;

import java.time.LocalDate;

/**
 * Created on 03/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Rates {

    private final LocalDate validityDate;
    private final double euroToChf;

    public Rates(LocalDate validityDate, double euroToChf) {
        this.validityDate = validityDate;
        this.euroToChf = euroToChf;
    }

    public double getEuroToChf() {
        return euroToChf;
    }

    public LocalDate getValidityDate() {
        return validityDate;
    }

    @Override
    public String toString() {
        return "Rates{" +
                "validityDate=" + validityDate +
                ", euroToChf=" + euroToChf +
                '}';
    }
}
