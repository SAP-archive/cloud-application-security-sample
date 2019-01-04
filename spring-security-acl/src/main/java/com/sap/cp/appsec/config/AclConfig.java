package com.sap.cp.appsec.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Configuration
public class AclConfig {
    private GenericConversionService conversionService;

    public AclConfig() {
        conversionService = new GenericConversionService();
        conversionService.addConverter(String.class, Long.class, new StringToLongConverter());
    }

    @Bean
    public MutableAclService aclService(HikariDataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        JdbcMutableAclService jdbcAclService = new PostgresJdbcMutableAclService(
                dataSource, lookupStrategy, aclCache);

        jdbcAclService.setAclClassIdSupported(true);
        jdbcAclService.setConversionService(conversionService);

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
        strategy.setConversionService(conversionService);
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

        PostgresJdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
            super(dataSource, lookupStrategy, aclCache);
        }

        public List<String> getAllSidsWithPrefix(String prefix) {
            return jdbcTemplate.queryForList(selectSidsWithPrefix, String.class, prefix + "_%");
        }

        /**
         * TODO: Remove in case pull request was accepted:
         * https://github.com/spring-projects/spring-security/pull/6050
         */

        // copy of this JdbcMutableAclService.selectObjectIdentityPrimaryKey
        private String selectObjectIdentityPrimaryKey = "select acl_object_identity.id from acl_object_identity, acl_class "
                + "where acl_object_identity.object_id_class = acl_class.id and acl_class.class=? "
                + "and acl_object_identity.object_id_identity = ?";

        // copy of JdbcMutableAclService.insertObjectIdentity
        private String insertObjectIdentity = "insert into acl_object_identity (object_id_class, object_id_identity, owner_sid, entries_inheriting) values (?, ?, ?, ?)";

        // copy of JdbcAclService.findChildrenSql
        private String findChildrenSql = "select obj.object_id_identity as obj_id, class.class as class from acl_object_identity obj, acl_object_identity parent, acl_class class where obj.parent_object = parent.id and obj.object_id_class = class.id and parent.object_id_identity = ? and parent.object_id_class = (select id FROM acl_class where acl_class.class = ?)";

        @Override
        protected Long retrieveObjectIdentityPrimaryKey(ObjectIdentity oid) {
            try {
                return this.jdbcTemplate.queryForObject(this.selectObjectIdentityPrimaryKey, Long.class, new Object[]{oid.getType(), "" + oid.getIdentifier()});
            } catch (DataAccessException var3) {
                return null;
            }
        }

        @Override
        public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
            Object[] args = new Object[]{"" + parentIdentity.getIdentifier(), parentIdentity.getType()};
            List<ObjectIdentity> objects = this.jdbcTemplate.query(this.findChildrenSql, args, new RowMapper<ObjectIdentity>() {
                public ObjectIdentity mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String javaType = rs.getString("class");
                    Serializable identifier = (Serializable) rs.getObject("obj_id");
//                    identifier = JdbcAclService.this.aclClassIdUtils.identifierFrom(identifier, rs); //can not be overwritten
                    return new ObjectIdentityImpl(javaType, identifier);
                }
            });
            return objects.size() == 0 ? null : objects;
        }

        @Override
        protected void createObjectIdentity(ObjectIdentity object, Sid owner) {
            Long sidId = this.createOrRetrieveSidPrimaryKey(owner, true);
            Long classId = this.createOrRetrieveClassPrimaryKey(object.getType(), true, object.getIdentifier().getClass());
            this.jdbcTemplate.update(this.insertObjectIdentity, new Object[]{classId, "" + object.getIdentifier(), sidId, Boolean.TRUE});
        }
    }

    /**
     * TODO: Remove in case pull request was accepted:
     * https://github.com/spring-projects/spring-security/pull/6141
     */
    private static class StringToLongConverter implements Converter<String, Long> {

        @Override
        public Long convert(String idAsString) {
            if (idAsString == null) {
                throw new ConversionFailedException(TypeDescriptor.valueOf(String.class),
                        TypeDescriptor.valueOf(String.class), null, null);

            }
            return Long.parseLong(idAsString);
        }
    }
}