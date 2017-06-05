package com.wx.invoicefx.view.other.animation;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class Animator {

    private static AnimatorInterface animator = new DefaultAnimator();

    public static AnimatorInterface instance() {
        return animator;
    }

    private Animator() {}

}
