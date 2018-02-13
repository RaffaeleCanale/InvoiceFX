package com.wx.invoicefx.util.concurrent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.06.17.
 */
public abstract class ThrottledTask implements Runnable {

    private final int interval;

    private final Timer timer = new Timer(true);
    private TimerTask task;


    public ThrottledTask(int interval) {
        this.interval = interval;

    }

    public void execute() {
        if (task != null) {
            task.cancel();
        }

        task = new TimerTask() {
            @Override
            public void run() {
                ThrottledTask.this.run();
                task = null;
            }
        };

        timer.schedule(task, interval);
    }

}
