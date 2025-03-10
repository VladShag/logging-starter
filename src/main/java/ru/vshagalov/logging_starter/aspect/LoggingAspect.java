package ru.vshagalov.logging_starter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import ru.vshagalov.logging_starter.config.LoggingProperties;

import java.util.Arrays;

/**
 * Аспект логирования.
 */
@Aspect
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final String EXCEPTION_LOG_MESSAGE = "Метод {} выбросил исключение: {}";
    private final LoggingProperties loggingProperties;

    @Autowired
    public LoggingAspect(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Before("@annotation(ru.vshagalov.logging_starter.annotation.LogBefore)")
    public void logBefore(JoinPoint joinPoint) {
        logWithLevel(String.format("Происходит вызов метода %s с параметрами %s.",
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs())));
    }

    @AfterThrowing(pointcut = "@annotation(ru.vshagalov.logging_starter.annotation.LogAfterThrowing)", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        if (isLoggingEnabled()) {
            logger.error(EXCEPTION_LOG_MESSAGE,
                    joinPoint.getSignature().getName(), error.getMessage());
        }
    }

    @AfterReturning(pointcut = "@annotation(ru.vshagalov.logging_starter.annotation.LogAfterReturning)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logWithLevel(String.format("Успешное завершение работы метода %s, результат: %s",
                joinPoint.getSignature().getName(), result));
    }

    @Around("@annotation(ru.vshagalov.logging_starter.annotation.LogAround)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
            String methodName = joinPoint.getSignature().getName();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logWithLevel(String.format("Происходит вызов метода %s с параметрами %s",
                    methodName, Arrays.toString(joinPoint.getArgs())));
            try {
                Object result = joinPoint.proceed();
                stopWatch.stop();
                logWithLevel(String.format("Успешное завершение метода %s с результатом %s за %s секунд.",
                        methodName, result, stopWatch.getTotalTimeSeconds()));
                return result;
            } catch (Exception e) {
                logger.error(EXCEPTION_LOG_MESSAGE,
                        joinPoint.getSignature().getName(), e.getMessage());
                throw e;
            }
    }

    @Around("@annotation(ru.vshagalov.logging_starter.annotation.ControllerMethodLog)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String controllerName = joinPoint.getTarget().getClass().getName();
        logWithLevel(String.format("Контроллер %s принял запрос в метод %s с входящими параметрами %s",
                controllerName, methodName, Arrays.toString(joinPoint.getArgs())));
        try {
            Object result = joinPoint.proceed();
            logWithLevel(String.format("Контроллер %s успешно отработал метод %s и вернул %s.",
                    controllerName, methodName, result));
            return result;
        } catch (Exception e) {
            logger.error(EXCEPTION_LOG_MESSAGE,
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        }
    }

    private void logWithLevel(String message) {
        if (!isLoggingEnabled()) {
            return;
        }
        switch (loggingProperties.getLevel().toUpperCase()) {
            case "DEBUG":
                logger.debug(message);
                break;
            case "WARN":
                logger.warn(message);
                break;
            case "ERROR":
                logger.error(message);
                break;
            case "INFO":
            default:
                logger.info(message);
                break;
        }
    }

    private boolean isLoggingEnabled() {
        return loggingProperties.isEnabled();
    }
}
