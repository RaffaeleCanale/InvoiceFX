package com.wx.invoicefx.ui.animation.task;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 19.06.17.
 */
public class TimelineTask extends AnimatorTask {

    private final Timeline timeline;

    public TimelineTask(Timeline timeline) {
        this.timeline = timeline;

        timeline.setOnFinished(e -> finished());
    }

    @Override
    public void run() {
        timeline.play();
    }

    @Override
    protected void finished() {
        super.finished();


    }

    private static Duration getLastDuration(Timeline timeline) {
        return timeline.getKeyFrames().stream()
                .map(KeyFrame::getTime)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    private static List<KeyFrame> shiftKeyFrames(Timeline timeline, Duration duration) {
        return timeline.getKeyFrames().stream()
                .map(keyFrame -> new KeyFrame(keyFrame.getTime().add(duration), keyFrame.getName(), keyFrame.getOnFinished(), keyFrame.getValues()))
                .collect(Collectors.toList());
    }
}
