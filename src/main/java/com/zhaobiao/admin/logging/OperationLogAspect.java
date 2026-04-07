package com.zhaobiao.admin.logging;

import com.zhaobiao.admin.entity.OperationLog;
import com.zhaobiao.admin.security.LoginUser;
import com.zhaobiao.admin.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(com.zhaobiao.admin.logging.OperationLogRecord)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLogRecord record = signature.getMethod().getAnnotation(OperationLogRecord.class);
        try {
            Object result = joinPoint.proceed();
            saveLog(record, true, "SUCCESS");
            return result;
        } catch (Throwable ex) {
            saveLog(record, false, ex.getMessage());
            throw ex;
        }
    }

    private void saveLog(OperationLogRecord record, boolean success, String detail) {
        OperationLog operationLog = new OperationLog();
        operationLog.setModule(record.module());
        operationLog.setAction(record.action());
        operationLog.setSuccess(success);
        operationLog.setDetail(truncate(detail));

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUri(request.getRequestURI());
            operationLog.setIpAddress(resolveClientIp(request));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            operationLog.setOperatorUsername(((LoginUser) authentication.getPrincipal()).getUsername());
        } else if (authentication != null) {
            operationLog.setOperatorUsername(authentication.getName());
        }

        operationLogService.save(operationLog);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && forwarded.trim().length() > 0) {
            return truncate(forwarded.split(",")[0].trim());
        }
        return truncate(request.getRemoteAddr());
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
