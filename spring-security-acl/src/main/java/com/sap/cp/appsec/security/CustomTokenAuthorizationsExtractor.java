package com.sap.cp.appsec.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.cloud.security.xsuaa.extractor.AuthoritiesExtractor;
import com.sap.cloud.security.xsuaa.extractor.LocalAuthoritiesExtractor;
import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cloud.security.xsuaa.token.XsuaaToken;
import com.sap.cp.appsec.domain.AclAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomTokenAuthorizationsExtractor implements AuthoritiesExtractor {
    private AuthoritiesExtractor authoritiesExtractor;
    protected AclAttribute[] aclAttributes;

    public CustomTokenAuthorizationsExtractor(String appId, AclAttribute... aclAttributes) {
        authoritiesExtractor = new LocalAuthoritiesExtractor(appId);
        this.aclAttributes = aclAttributes;
    }

    public Collection<GrantedAuthority> getAuthorities(XsuaaToken jwt) {
        Collection<GrantedAuthority> authorities = authoritiesExtractor.getAuthorities(jwt);
        authorities.addAll(getCustomAuthorities(jwt));
        return authorities;
    }

    private Collection<GrantedAuthority> getCustomAuthorities(Token token) {
        Set<GrantedAuthority> newAuthorities = new HashSet<>();
        for (AclAttribute aclAttribute : aclAttributes) {
            String[] xsUserAttributeValues = token.getXSUserAttribute(aclAttribute.getXSUserAttributeName());
            if (xsUserAttributeValues != null) {
                for (String xsUserAttributeValue : xsUserAttributeValues) {
                    newAuthorities.add(new SimpleGrantedAuthority(aclAttribute.getSidForAttributeValue(xsUserAttributeValue)));
                }
            }
        }
        return newAuthorities;
    }

    private static String getSidForAttributeValue(String attributeName, String attributeValue) {
        return "ATTR:" + attributeName.toUpperCase() + "=" + attributeValue;
    }

}
