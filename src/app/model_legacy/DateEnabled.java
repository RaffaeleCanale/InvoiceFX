package app.model_legacy;

import javax.xml.bind.annotation.XmlEnum;

/**
 * A client item may have two dates (from and to, describing a period), one or no date. Each case can be described by
 * this enum.
 * <p>
 * Created on 22/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
@XmlEnum
public enum DateEnabled {
    BOTH,
    ONLY_FROM,
    NONE
}
