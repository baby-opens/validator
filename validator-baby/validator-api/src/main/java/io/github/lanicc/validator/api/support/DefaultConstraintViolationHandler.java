package io.github.lanicc.validator.api.support;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
public class DefaultConstraintViolationHandler implements ConstraintViolationHandler {

    @Override
    public void handle(Set<ConstraintViolation<Object>> result) throws Exception {
        for (ConstraintViolation<Object> violation : result) {
            String message = violation.getMessage();
            String property = violation.getPropertyPath().toString();
            Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
            String errorMessage = parseMessage(message, property, annotation);
            throw new ConstraintViolationException(errorMessage, result);
        }
    }

    private String parseMessage(String message, String property, Annotation annotation) {
        return String.format("%s %s", property, message);
    }

}
