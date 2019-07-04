package com.sap.cp.appsec.config;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.XsuaaServiceConfigurationDefault;
import com.sap.cloud.security.xsuaa.XsuaaServicePropertySourceFactory;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import com.sap.cloud.security.xsuaa.token.authentication.XsuaaJwtDecoderBuilder;
import com.sap.cp.appsec.domain.AclAttribute;
import com.sap.cp.appsec.security.CustomTokenAuthorizationsExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private XsuaaServiceConfiguration xsuaaServiceConfiguration;

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity http) throws Exception {

		// @formatter:off
		http
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
				.authorizeRequests()
				// enable OAuth2 checks
				.antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**", "/api-docs/**").permitAll() // this should allow swagger to operate
				.antMatchers("/**/*.js", "/**/*.json", "/**/*.xml", "/**/*.html").permitAll()
				// acl endpoints
				.antMatchers("/api/v1/ads/acl/**").authenticated()
				// public endpoints
				.antMatchers("/actuator/**").permitAll()
				.antMatchers("/api/v1/attribute/**").permitAll()
				.anyRequest().denyAll() // deny anything not configured above
			.and()
				.oauth2ResourceServer()
				.jwt()
				.jwtAuthenticationConverter(getJwtAuthoritiesConverter()); // customizes how GrantedAuthority s are derived from a Jwt
		// @formatter:on
	}

	/**
	 * Customizes how GrantedAuthority are derived from a Jwt
	 */
	Converter<Jwt, AbstractAuthenticationToken> getJwtAuthoritiesConverter() {
		return new TokenAuthenticationConverter(new CustomTokenAuthorizationsExtractor(xsuaaServiceConfiguration.getAppId(), AclAttribute.values()));
	}
}