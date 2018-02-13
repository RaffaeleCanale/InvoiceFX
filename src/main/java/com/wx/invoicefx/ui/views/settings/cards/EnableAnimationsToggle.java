package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.DefaultAnimator;
import com.wx.invoicefx.ui.animation.DisabledAnimator;
import com.wx.invoicefx.ui.components.settings.PropertyTogglePane;
import javafx.beans.NamedArg;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.ENABLE_ANIMATIONS;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class EnableAnimationsToggle extends PropertyTogglePane {


    public EnableAnimationsToggle(@NamedArg("text") String text) {
        super(text);

        bindWith(AppResources.localPreferences().booleanProperty(ENABLE_ANIMATIONS));
        getSwitchProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Animator.setInstance(new DefaultAnimator());
            } else {
                Animator.setInstance(new DisabledAnimator());
            }
        });
    }

}
