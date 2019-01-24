package com.sap.cp.appsec.security;

import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;
import com.sap.cp.appsec.domain.AclAttribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomTokenAuthorizationsExtractor extends TokenAuthenticationConverter {

    protected AclAttribute[] aclAttributes;

    public CustomTokenAuthorizationsExtractor(String appId, AclAttribute... aclAttributes) {
        super(appId);
        setLocalScopeAsAuthorities(true);
        this.aclAttributes = aclAttributes;
    }

    @Override
    protected Collection<String> getCustomAuthorities(Token token) {
        Set<String> newAuthorities = new HashSet<>();
        for (AclAttribute aclAttribute : aclAttributes) {
            String[] xsUserAttributeValues = token.getXSUserAttribute(aclAttribute.getXSUserAttributeName());
            if (xsUserAttributeValues != null) {
                for (String xsUserAttributeValue : xsUserAttributeValues) {
                    newAuthorities.add(aclAttribute.getSidForAttributeValue(xsUserAttributeValue));
                }
            }
        }
        return newAuthorities;
    }
}
