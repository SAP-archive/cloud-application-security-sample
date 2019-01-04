--- Business Data
INSERT INTO ADVERTISEMENT (ID, TITLE, CONTACT, CREATED_AT, CREATED_BY, VERSION, IS_PUBLISHED) VALUES
    (301, 'Ads 1', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (302, 'Ads 2', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (303, 'Ads 3', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (304, 'Ads 4', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (305, 'Ads 5', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (306, 'Ads 6', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true),
    (307, 'Ads 7', 'tester@email.com','2018-08-17 17:00:00.000','user/userIdp/adOwner', 0, true);

--- ACL Data
INSERT INTO ACL_CLASS (ID, CLASS, CLASS_ID_TYPE) VALUES
    (2010, 'com.sap.cp.appsec.domain.Advertisement', '');

INSERT INTO ACL_SID (ID, PRINCIPAL, SID) VALUES
    (3010, true, 'user/userIdp/adOwner');

INSERT INTO ACL_OBJECT_IDENTITY (ID, OBJECT_ID_CLASS, OBJECT_ID_IDENTITY, PARENT_OBJECT, OWNER_SID, ENTRIES_INHERITING) VALUES
    (1301, 2010, '301', NULL,         3010, true),
    (1302, 2010, '302', 100000001200, 3010, true),
    (1303, 2010, '303', 100000001201, 3010, true),
    (1304, 2010, '304', 100000001201, 3010, true),
    (1305, 2010, '305', 100000001202, 3010, true),
    (1306, 2010, '306', 100000001100, 3010, true),
    (1307, 2010, '307', 100000001101, 3010, true);

INSERT INTO ACL_ENTRY (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
    (5000, 1301, 0, 3010, 16, true, false, false),
    (5001, 1301, 1, 3010, 1,  true, false, false),
    (5002, 1301, 2, 3010, 2,  true, false, false);

