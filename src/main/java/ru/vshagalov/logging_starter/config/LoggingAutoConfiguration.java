package ru.vshagalov.logging_starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.vshagalov.logging_starter.aspect.LoggingAspect;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "logging-starter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    public LoggingAspect loggingAspect(LoggingProperties loggingProperties) {
        return new LoggingAspect(loggingProperties);
    }
}
