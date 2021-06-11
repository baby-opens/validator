package io.github.lanicc.validator.api.support;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Created on 2021/6/5.
 *
 * @author lan
 * @since 2.0.0
 */
public interface ConstraintViolationHandler {

    /**
     * 处理参数校验的结果，如果没有抛出异常，将会正常执行
     *
     * @param result 校验结果
     * @throws Exception 异常
     */
    void handle(Set<ConstraintViolation<Object>> result) throws Exception;
}
