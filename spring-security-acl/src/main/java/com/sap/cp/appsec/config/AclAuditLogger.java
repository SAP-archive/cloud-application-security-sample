package com.sap.cp.appsec.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.security.acls.model.AuditableAcl;
import org.springframework.util.Assert;

/**
 * You can specify an audit logger that is able to write audit-relevant logs in case of granted / un-granted access
 * to an access control list (ACL) or more precisely to an {@link AuditableAccessControlEntry} (ACE).
 *
 * Note: by default, an ACL is not configured to audit successful or failed access.
 * You need to specify that in context of the {@link AuditableAcl} using updateAuditing method .
 *
 * By default #logIfNeeded is only called in a few cases, e.g. in case of owner change,
 * change of audit log settings or general change ({@link AclAuthorizationStrategy}.
 *
 * Additionlly note that #logIfNeeded with `isGranted=false` is only called when ACE explicitly specifies "granted=false"
 * and not when a permission (ACE) is missing.
 *
 */
public class AclAuditLogger implements AuditLogger {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void logIfNeeded(boolean isGranted, AccessControlEntry ace) {
		Assert.notNull(ace, "AccessControlEntry required");

		if (ace instanceof AuditableAccessControlEntry) {
			AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;

			// log only in case ACE configures auditSuccess = true
			if (isGranted && auditableAce.isAuditSuccess()) {
				logger.info("GRANTED due to ACE: " + ace);
			}
			// log only in case ACE configures auditFailure = true
			if (!isGranted && auditableAce.isAuditFailure()) {
				logger.warn("DENIED due to ACE: " + ace);
			}
		}
	}

	public void logGrantPermission(AccessControlEntry ace) {
		Assert.notNull(ace, "AccessControlEntry required");
		logger.info("CREATED ACE: " + ace);
	}

	public void logRemovePermission(AccessControlEntry ace) {
		Assert.notNull(ace, "AccessControlEntry required");
		logger.info("REMOVED ACE: " + ace);
	}
}
