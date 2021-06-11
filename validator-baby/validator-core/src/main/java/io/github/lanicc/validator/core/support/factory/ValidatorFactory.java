package io.github.lanicc.validator.core.support.factory;

import io.github.lanicc.validator.api.annotation.constraint.EnumInclude;
import io.github.lanicc.validator.core.support.constraint.EnumConstraintValidator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created on 2021/6/10.
 *
 * @author lan
 * @since 2.0.0
 */
public class ValidatorFactory {

    public static Validator getValidator() {

        Locale.setDefault(Locale.CHINESE);

        HibernateValidatorConfiguration configuration =
                Validation.byProvider(HibernateValidator.class)
                        .configure()
                        .failFast(true)
                        .allowParallelMethodsDefineParameterConstraints(true)
                        .allowOverridingMethodAlterParameterConstraint(true);

        registerIdentityConstraintMapping(configuration);
        registerIdentityMessageInterpolator(configuration);

        javax.validation.ValidatorFactory validatorFactory = configuration.buildValidatorFactory();

        return validatorFactory.getValidator();
    }

    private static void registerIdentityConstraintMapping(HibernateValidatorConfiguration configuration) {
        //TODO  API挺丰富，需要弄清楚
        ConstraintMapping enumIncludeMapping = configuration.createConstraintMapping();
        enumIncludeMapping.constraintDefinition(EnumInclude.class)
                .validatedBy(EnumConstraintValidator.class);
        configuration.addMapping(enumIncludeMapping);
    }

    private static void registerIdentityMessageInterpolator(HibernateValidatorConfiguration configuration) {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("@com.souche.component.sharing.validation.annotation.constraint.EnumInclude.message", "枚举值错误");
        configuration.messageInterpolator(new MessageInterpolator(msgMap));
    }
}
