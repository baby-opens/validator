package io.github.lanicc.validator.api.annotation;

import java.lang.annotation.*;

/**
 * 起这个名称是因为org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver#validateIfApplicable，fuck springmvc
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BabyValid {

}
