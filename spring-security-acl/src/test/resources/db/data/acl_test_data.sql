--- Business Data

INSERT INTO ADVERTISEMENT (ID, TITLE, CONTACT, CREATED_AT, CREATED_BY, VERSION, IS_PUBLISHED) VALUES
    (777, 'Ads 1','tester@email.com','2018-08-17 10:06:54.742','ownerAndAdmin', 0, false);

--- ACL Data

INSERT INTO ACL_CLASS (ID, CLASS,CLASS_ID_TYPE) VALUES
    (200, 'com.sap.cp.appsec.domain.Advertisement','java.lang.Long');

INSERT INTO ACL_SID (ID, PRINCIPAL, SID) VALUES
    (300, true, 'ownerAndAdmin'),
    (301, true, 'noOwnerButReader'),
    (302, true, 'noOwnerButAdmin');

INSERT INTO ACL_OBJECT_IDENTITY (ID, OBJECT_ID_CLASS, OBJECT_ID_IDENTITY, PARENT_OBJECT, OWNER_SID, ENTRIES_INHERITING) VALUES
    (400, 200, '777', NULL, 300, true);

INSERT INTO ACL_ENTRY (ID, ACL_OBJECT_IDENTITY, ACE_ORDER, SID, MASK, GRANTING, AUDIT_SUCCESS, AUDIT_FAILURE) VALUES
    (500, 400, 0, 300, 16,  true, false, false),
    (501, 400, 1, 300, 1,   true, false, false),
    (502, 400, 2, 300, 2,   true, false, false),
    (503, 400, 3, 301, 1,   true, false, false),
    (504, 400, 4, 302, 16,  true, false, false);
