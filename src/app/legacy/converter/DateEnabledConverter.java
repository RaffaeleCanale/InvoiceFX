package app.legacy.converter;

import app.model.DateEnabled;
import com.wx.util.representables.TypeCaster;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.06.16.
 */
public class DateEnabledConverter implements TypeCaster<DateEnabled, app.legacy.model.DateEnabled> {

    @Override
    public DateEnabled castIn(app.legacy.model.DateEnabled dateEnabled) throws ClassCastException {
        switch (dateEnabled) {
            case BOTH:
                return DateEnabled.BOTH;
            case NONE:
                return DateEnabled.NONE;
            case ONLY_FROM:
                return DateEnabled.ONLY_FROM;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public app.legacy.model.DateEnabled castOut(DateEnabled dateEnabled) throws ClassCastException {
        switch (dateEnabled) {
            case BOTH:
                return app.legacy.model.DateEnabled.BOTH;
            case NONE:
                return app.legacy.model.DateEnabled.NONE;
            case ONLY_FROM:
                return app.legacy.model.DateEnabled.ONLY_FROM;
            default:
                throw new AssertionError();
        }
    }
}
