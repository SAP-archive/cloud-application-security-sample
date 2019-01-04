package com.sap.cp.appsec.config;

import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.xs2.security.container.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
    @Bean
    AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    private static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            String user;
            Logger logger = LoggerFactory.getLogger(getClass());

            Token token = SecurityContext.getToken();
            user = token.getLogonName();
            logger.info("token for user " + user + " initialized");
            return Optional.ofNullable(user);
        }
    }
}
