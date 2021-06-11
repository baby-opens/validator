package io.github.lanicc.validator.core.config;

import io.github.lanicc.validator.core.support.MethodValidationPostProcessor;
import io.github.lanicc.validator.core.support.factory.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
@Configuration
public class ValidationConfig {

    @Bean
    public MethodValidationPostProcessor componentMethodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.setValidator(ValidatorFactory.getValidator());
        return postProcessor;
    }

}
