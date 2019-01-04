package com.sap.cp.appsec.mock;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

public class MockPostProcessor implements EnvironmentPostProcessor, DisposableBean {

    private final MockAuthorizationServer propertySource = new MockAuthorizationServer();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("uaamock"))) {
            environment.getPropertySources().addFirst(this.propertySource);
        }
    }

    @Override
    public void destroy() throws Exception {
        this.propertySource.destroy();
    }
}