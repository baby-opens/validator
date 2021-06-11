package io.github.lanicc.validator.core.support;

import io.github.lanicc.validator.api.annotation.BabyValid;
import io.github.lanicc.validator.api.annotation.BabyValidated;
import io.github.lanicc.validator.api.support.ConstraintViolationHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
public class MethodValidationInterceptor implements MethodInterceptor {

    private final Validator validator;

    /**
     * Create a new MethodValidationInterceptor using the given JSR-303 Validator.
     *
     * @param validator the JSR-303 Validator to use
     */
    public MethodValidationInterceptor(Validator validator) {
        this.validator = validator;
    }


    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (isFactoryBeanMetadataMethod(invocation.getMethod())) {
            return invocation.proceed();
        }

        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        Method methodToValidate = findBridgedMethod(target, invocation.getMethod());

        BabyValidated babyValidated = determineValidatedAnnotation(target, methodToValidate);
        Class<?>[] groups = babyValidated.value();
        Class<? extends ConstraintViolationHandler> handler = babyValidated.handler();
        ConstraintViolationHandler violationHandler = handler.newInstance();

        // Standard Bean Validation 1.1 API
        ExecutableValidator execVal = this.validator.forExecutables();
        Set<ConstraintViolation<Object>> result;

        Object[] arguments = invocation.getArguments();

        //validateParameters
        try {
            result = execVal.validateParameters(target, methodToValidate, arguments, groups);
        } catch (IllegalArgumentException | ConstraintDeclarationException ex) {
            // Probably a generic type mismatch between interface and impl as reported in SPR-12237 / HV-1011
            // Let's try to find the bridged method on the implementation class...
            methodToValidate = BridgeMethodResolver.findBridgedMethod(
                    ClassUtils.getMostSpecificMethod(invocation.getMethod(), target.getClass()));

            babyValidated = determineValidatedAnnotation(target, methodToValidate);
            groups = babyValidated.value();
            handler = babyValidated.handler();
            violationHandler = handler.newInstance();

            result = execVal.validateParameters(target, methodToValidate, arguments, groups);
        }
        if (!result.isEmpty()) {
            violationHandler.handle(result);
        }

        //Parameter object
        Parameter[] parameters = methodToValidate.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(BabyValid.class)) {
                result = validator.validate(arguments[i], groups);
                if (!result.isEmpty()) {
                    violationHandler.handle(result);
                }
            }
        }

        Object returnValue = invocation.proceed();

        result = execVal.validateReturnValue(target, methodToValidate, returnValue, groups);
        if (!result.isEmpty()) {
            violationHandler.handle(result);
        }

        return returnValue;
    }

    static final ConcurrentMap<String, Method> bridgeMethodMap = new ConcurrentHashMap<>();

    private Method findBridgedMethod(Object target, Method methodToValidate) {
        String key = key(target.getClass(), methodToValidate);
        Method m = bridgeMethodMap.get(key);
        if (m != null) {
            return m;
        }
        Class<?>[] interfaces = target.getClass().getInterfaces();
        itfLoop:
        {
            for (Class<?> itf : interfaces) {
                Method[] methods = itf.getDeclaredMethods();
                for (Method method : methods) {
                    if (!method.isBridge() && !method.equals(methodToValidate) &&
                            method.getName().equals(methodToValidate.getName()) &&
                            method.getParameterCount() == methodToValidate.getParameterCount()) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Class<?>[] validateParameterTypes = methodToValidate.getParameterTypes();
                        match:
                        {

                            for (int i = 0; i < parameterTypes.length; i++) {
                                if (!validateParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                                    break match;
                                }
                            }

                            bridgeMethodMap.put(key, method);

                            m = method;
                            break itfLoop;
                        }
                    }
                }
            }
        }
        if (m == null) {
            return methodToValidate;
        }
        return m;
    }

    private String key(Class<?> clazz, Method method) {
        return String.format("%s#%s#%s", clazz.getName(), method.getName(), Stream.of(method.getParameterTypes()).map(Class::getTypeName).reduce(String::concat).orElse(""));
    }

    private boolean isFactoryBeanMetadataMethod(Method method) {
        Class<?> clazz = method.getDeclaringClass();

        // Call from interface-based proxy handle, allowing for an efficient check?
        if (clazz.isInterface()) {
            return ((clazz == FactoryBean.class || clazz == SmartFactoryBean.class) &&
                    !method.getName().equals("getObject"));
        }

        // Call from CGLIB proxy handle, potentially implementing a FactoryBean method?
        Class<?> factoryBeanType = null;
        if (SmartFactoryBean.class.isAssignableFrom(clazz)) {
            factoryBeanType = SmartFactoryBean.class;
        } else if (FactoryBean.class.isAssignableFrom(clazz)) {
            factoryBeanType = FactoryBean.class;
        }
        return (factoryBeanType != null && !method.getName().equals("getObject") &&
                ClassUtils.hasMethod(factoryBeanType, method));
    }


    protected BabyValidated determineValidatedAnnotation(Object target, Method methodToInvalidate) {
        BabyValidated babyValidatedAnn = AnnotationUtils.findAnnotation(methodToInvalidate, BabyValidated.class);
        if (babyValidatedAnn == null) {
            Assert.state(target != null, "Target must not be null");
            babyValidatedAnn = AnnotationUtils.findAnnotation(target.getClass(), BabyValidated.class);
        }
        return babyValidatedAnn;
    }

}
