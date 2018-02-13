package com.wx.invoicefx.ui.animation.task;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 19.06.17.
 */
public abstract class AnimatorTask implements Runnable {

    public static AnimatorTask noop() {
        return new AnimatorTask() {
                    @Override
                    public void run() {
                        finished();
                    }
                };
    }

    private AnimatorTask next;

    public AnimatorTask then(AnimatorTask next) {
        if (this.next == null) {
            this.next = next;
        } else {
            this.next.then(next);
        }

        return this;
    }

    public AnimatorTask then(Runnable runnable) {
        return then(new ImmediateTask(runnable));
    }

    protected void finished() {
        if (next != null) {
            next.run();
        }
    }
}
