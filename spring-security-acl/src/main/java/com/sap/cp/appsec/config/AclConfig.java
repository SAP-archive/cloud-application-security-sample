package com.sap.cp.appsec.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class AclConfig {

	@Bean
	public MutableAclService aclService(HikariDataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
		JdbcMutableAclService jdbcAclService = new PostgresJdbcMutableAclService(
				dataSource, lookupStrategy, aclCache);

		jdbcAclService.setAclClassIdSupported(true);

		if (dataSource.getDriverClassName().equals("org.postgresql.Driver")) {
			// because of PostgreSQL as documented here:
			// https://docs.spring.io/spring-security/site/docs/current/reference/html5/#postgresql
			jdbcAclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
			jdbcAclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
		}
		return jdbcAclService;
	}

	@Bean //implements hasPermission annotations
	public PermissionEvaluator permissionEvaluator(MutableAclService aclService) {
		return new AclPermissionEvaluator(aclService);
	}

	@Bean
	public LookupStrategy lookupStrategy(DataSource dataSource) {
		BasicLookupStrategy strategy = new BasicLookupStrategy(
				dataSource,
				aclCache(),
				aclAuthorizationStrategy(),
				new AclAuditLogger()
		);

		strategy.setAclClassIdSupported(true);
		strategy.setPermissionFactory(new DefaultPermissionFactory(BasePermission.class));
		return strategy;
	}

	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy() {
		return new AclAuthorizationStrategyImpl(
				new SimpleGrantedAuthority("ROLE_ACL_ADMIN"));
	}

	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy() {
		return new DefaultPermissionGrantingStrategy(
				new AclAuditLogger());
	}

	// Cache Setup

	@Bean
	public AclCache aclCache() {
		return new EhCacheBasedAclCache(
				aclEhCacheFactoryBean().getObject(),
				permissionGrantingStrategy(),
				aclAuthorizationStrategy()
		);
	}

	@Bean
	public EhCacheFactoryBean aclEhCacheFactoryBean() {
		EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
		ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
		ehCacheFactoryBean.setCacheName("acl_cache");
		return ehCacheFactoryBean;
	}

	@Bean
	public EhCacheManagerFactoryBean aclCacheManager() {
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setShared(true);
		return cacheManagerFactoryBean;
	}


	public static class PostgresJdbcMutableAclService extends JdbcMutableAclService {
		private String selectSidsWithPrefix = "select acl_sid.sid from acl_sid "
				+ "where acl_sid.sid like ? and "
				+ "      acl_sid.principal = false";

		public PostgresJdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
			super(dataSource, lookupStrategy, aclCache);
		}

		public List<String> getAllSidsWithPrefix(String prefix) {
			return jdbcOperations.queryForList(selectSidsWithPrefix, String.class, prefix + "_%");
		}
	}
}