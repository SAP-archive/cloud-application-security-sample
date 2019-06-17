package com.sap.cp.appsec.config;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.XsuaaServiceConfigurationDefault;
import com.sap.cloud.security.xsuaa.XsuaaServicePropertySourceFactory;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import com.sap.cloud.security.xsuaa.token.authentication.XsuaaJwtDecoderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@PropertySource(factory = XsuaaServicePropertySourceFactory.class, value = {""})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	XsuaaServiceConfiguration xsuaaServiceConfiguration;

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity http) throws Exception {

        http
            .sessionManagement()
            // session is created by approuter
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                // demand specific scopes depending on intended request
                .authorizeRequests()
                // enable OAuth2 checks
                .antMatchers("/**/*.js", "/**/*.json", "/**/*.xml", "/**/*.html").permitAll()
                .antMatchers(GET, "/api/v1/ads/**").hasAuthority("Display")
                .antMatchers(POST, "/api/v1/ads/**").hasAuthority("Update")
                .antMatchers(PUT, "/api/v1/ads/**").hasAuthority("Update")
                .antMatchers(GET, "/api/v1/attribute/**").permitAll()
                .antMatchers("/api/v1/**").authenticated()
                .antMatchers("/").authenticated()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/hystrix.stream").permitAll()
                .anyRequest().denyAll() // deny anything not configured above
            .and()
                .oauth2ResourceServer().jwt()
					.jwtAuthenticationConverter(getJwtAuthoritiesConverter());
	}

	@Bean
	XsuaaServiceConfiguration xsuaaConfiguration() {
		return new XsuaaServiceConfigurationDefault();
	}

	/**
	 * Configures Nimbus JWK (JSON Web Keys) endpoint, from where it can download the PEM-encoded RSA public key for
	 * decoding the token.
	 * And configures other Jwt Validators.
	 * @return jwt decoder
	 */
	@Bean
	JwtDecoder getJwtDecoder() {
		return new XsuaaJwtDecoderBuilder(xsuaaServiceConfiguration).build();
	}

	/**
	 * Customizes how GrantedAuthority are derived from a Jwt
	 *
	 * @returns jwt converter
	 */
	Converter<Jwt, AbstractAuthenticationToken> getJwtAuthoritiesConverter() {
		TokenAuthenticationConverter converter = new TokenAuthenticationConverter(xsuaaServiceConfiguration);
		converter.setLocalScopeAsAuthorities(true);
		return converter;
	}

}