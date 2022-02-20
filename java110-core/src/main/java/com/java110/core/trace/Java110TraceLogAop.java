package com.java110.core.trace;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.trace.TraceParamDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * trace log  api aop
 */
@Component
@Aspect
public class Java110TraceLogAop {

    @Pointcut("@annotation(com.java110.core.trace.Java110TraceLog)")
    public void dataProcess() {
    }

    //环绕通知,环绕增强，相当于MethodInterceptor
    @Around("dataProcess()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object out = null;
        TraceParamDto traceParamDto = new TraceParamDto();
        JSONObject paramIn = new JSONObject();
        JSONObject paramOut = new JSONObject();
        try {
            Object[] args = pjp.getArgs();
            for (int paramIndex = 0; paramIndex < args.length; paramIndex++) {
                paramIn.put("param" + paramIndex, args[paramIndex]);
            }
            traceParamDto.setReqParam(paramIn.toJSONString());
            out = pjp.proceed();
            if (paramOut != null) {
                paramOut.put("param", out);
            } else {
                paramOut.put("param", new JSONObject());
            }
            traceParamDto.setResParam(paramOut.toJSONString());
            Java110TraceFactory.putParams(traceParamDto);
        } catch (Throwable e) {
            throw e;
        } finally {
            return out;
        }
    }
}