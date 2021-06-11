package io.github.lanicc.validator.core.support.constraint;

import io.github.lanicc.validator.api.annotation.constraint.EnumInclude;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 2021/6/10.
 *
 * @author lan
 * @since 2.0.0
 */
public class EnumConstraintValidator implements ConstraintValidator<EnumInclude, Object> {
    private List<Function<Enum<?>, Object>> fieldGetters;
    private Class<? extends Enum<?>> enumClazz;

    @Override
    public void initialize(EnumInclude enumInclude) {
        enumClazz = enumInclude.value();
        fieldGetters =
                Stream.of(enumInclude.fields())
                        .map(s -> {
                            try {
                                return enumClazz.getDeclaredField(s);
                            } catch (NoSuchFieldException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .distinct()
                        .map(this::getter)
                        .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }
        Enum<?>[] constants = enumClazz.getEnumConstants();
        return Stream.of(constants)
                .anyMatch(e -> fieldGetters.stream()
                        .anyMatch(getter -> Objects.equals(getter.apply(e), value))
                );
    }

    protected Function<Enum<?>, Object> getter(Field field) {
        return e -> {
            try {
                field.setAccessible(true);
                return field.get(e);
            } catch (IllegalAccessException ee) {
                throw new RuntimeException(ee);
            } finally {
                field.setAccessible(false);
            }
        };
    }
}
