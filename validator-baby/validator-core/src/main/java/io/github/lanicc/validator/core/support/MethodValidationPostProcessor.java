package io.github.lanicc.validator.core.support;

import io.github.lanicc.validator.api.annotation.BabyValidated;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.annotation.Annotation;

/**
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
public class MethodValidationPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor implements InitializingBean {

    /**
     * 默认使用cglib代理，不适用jdk代理
     */
    public MethodValidationPostProcessor() {
        this.setProxyTargetClass(true);
    }

    private Class<? extends Annotation> validatedAnnotationType = BabyValidated.class;

    @Nullable
    private Validator validator;


    /**
     * Set the 'validated' annotation type.
     * The default validated annotation type is the {@link BabyValidated} annotation.
     * <p>This setter property exists so that developers can provide their own
     * (non-Spring-specific) annotation type to indicate that a class is supposed
     * to be validated in the sense of applying method validation.
     *
     * @param validatedAnnotationType the desired annotation type
     */
    public void setValidatedAnnotationType(Class<? extends Annotation> validatedAnnotationType) {
        Assert.notNull(validatedAnnotationType, "'validatedAnnotationType' must not be null");
        this.validatedAnnotationType = validatedAnnotationType;
    }

    /**
     * Set the JSR-303 Validator to delegate to for validating methods.
     * <p>Default is the default ValidatorFactory's default Validator.
     */
    public void setValidator(Validator validator) {
        // Unwrap to the native Validator with forExecutables support
        if (validator instanceof LocalValidatorFactoryBean) {
            this.validator = ((LocalValidatorFactoryBean) validator).getValidator();
        } else if (validator instanceof SpringValidatorAdapter) {
            this.validator = validator.unwrap(Validator.class);
        } else {
            this.validator = validator;
        }
    }

    /**
     * Set the JSR-303 ValidatorFactory to delegate to for validating methods,
     * using its default Validator.
     * <p>Default is the default ValidatorFactory's default Validator.
     *
     * @see ValidatorFactory#getValidator()
     */
    public void setValidatorFactory(ValidatorFactory validatorFactory) {
        this.validator = validatorFactory.getValidator();
    }


    @Override
    public void afterPropertiesSet() {
        Pointcut pointcut = new AnnotationMatchingPointcut(this.validatedAnnotationType, true);
        this.advisor = new DefaultPointcutAdvisor(pointcut, createMethodValidationAdvice(this.validator));
    }

    /**
     * Create AOP advice for method validation purposes, to be applied
     * with a pointcut for the specified 'validated' annotation.
     *
     * @param validator the JSR-303 Validator to delegate to
     * @return the interceptor to use (typically, but not necessarily,
     * a {@link MethodValidationInterceptor} or subclass thereof)
     * @since 4.2
     */
    protected Advice createMethodValidationAdvice(Validator validator) {
        return new MethodValidationInterceptor(validator);
    }
}
