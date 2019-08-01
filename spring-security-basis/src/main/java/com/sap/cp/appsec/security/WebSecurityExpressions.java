package com.sap.cp.appsec.security;

import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cp.appsec.domain.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * https://docs.spring.io/spring-security/site/docs/current/reference/html5/#el-access
 */
@Component("webSecurity") // Bean that offers methods that can be used within Spring Expression Language expressions
public class WebSecurityExpressions {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AdvertisementRepository repo;

    public boolean isCreatedBy(String id) {
        Token token = SpringSecurityContext.getToken();
        String currentUser = token.getLogonName();
        return currentUser == null || repo.existsByIdAndCreatedBy(new Long(id), token.getLogonName());
    }

    public boolean hasAttributeValue(String attributeName, String attributeValue) {
        Assert.notNull(attributeName, "requires attributeName");
        Assert.notNull(attributeValue, "requires attributeValue");

        boolean hasAttributeValue = false;
        Token token = SpringSecurityContext.getToken();
        String[] userAttributeValues = token.getXSUserAttribute(attributeName);
        if (userAttributeValues != null) {
            int index = Arrays.binarySearch(userAttributeValues, attributeValue);
            hasAttributeValue = index >= 0;
        }
        logger.info(String.format("Has user attribute %s = %s ? %s", attributeName, attributeValue, hasAttributeValue));
        return hasAttributeValue;
    }
}
