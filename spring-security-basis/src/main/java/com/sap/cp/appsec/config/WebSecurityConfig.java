package com.sap.cp.appsec.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
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