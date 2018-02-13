package com.wx.invoicefx.ui.animation.task;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 19.06.17.
 */
public class ImmediateTask extends AnimatorTask {

    private final Runnable runnable;

    public ImmediateTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
        finished();
    }

}
