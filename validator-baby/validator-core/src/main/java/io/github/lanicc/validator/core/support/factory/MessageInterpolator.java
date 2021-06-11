package io.github.lanicc.validator.core.support.factory;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2021/6/11.
 *
 * @author lan
 * @since 2.0.0
 */
public class MessageInterpolator extends ResourceBundleMessageInterpolator {

    //TODO  后续研究一下hibernate的messageTemplate
    private final Map<String, String> messageHolder;

    public MessageInterpolator() {
        this(new HashMap<>());
    }

    public MessageInterpolator(Map<String, String> messageHolder) {
        super();
        this.messageHolder = Objects.requireNonNull(messageHolder);
    }

    @Override
    public String interpolate(String message, Context context) {
        if (message.startsWith("@")) {
            return messageHolder.get(message);
        }
        return super.interpolate(message, context);
    }

    @Override
    public String interpolate(String message, Context context, Locale locale) {
        return interpolate(message, context);
    }

}
