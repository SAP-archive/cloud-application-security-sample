package com.sap.cp.appsec.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.util.Assert;

class AclAuditLogger implements AuditLogger {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void logIfNeeded(boolean granted, AccessControlEntry ace) {
        Assert.notNull(ace, "AccessControlEntry required");

        if (ace instanceof AuditableAccessControlEntry) {
            AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;

            if (granted && auditableAce.isAuditSuccess()) {
                logger.info("GRANTED due to ACE: " + ace);
            } else if (!granted && auditableAce.isAuditFailure()) {
                logger.warn("DENIED due to ACE: " + ace);
            }
        }
    }
}
