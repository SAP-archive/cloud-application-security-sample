--- Business Data

INSERT INTO ADVERTISEMENT (ID, TITLE, CONTACT, CREATED_AT, CREATED_BY, VERSION, IS_PUBLISHED) VALUES
    (871, 'Ads  1', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (872, 'Ads  2', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (873, 'Ads  3', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (874, 'Ads  4', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (875, 'Ads  5', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (876, 'Ads  6', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (877, 'Ads  7', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (878, 'Ads  8', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (879, 'Ads  9', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (880, 'Ads 10', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (881, 'Ads 10', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (882, 'Ads 12', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (883, 'Ads 13', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (884, 'Ads 14', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (885, 'Ads 15', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (886, 'Ads 16', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (887, 'Ads 17', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (888, 'Ads 18', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (889, 'Ads 19', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (890, 'Ads 20', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (891, 'Ads 21', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
--- Admin and adOwner only
    (892, 'Ads 22 (archived)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (893, 'Ads 23 (archived)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (894, 'Ads 24 (archived)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
    (895, 'Ads 25 (archived)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, true),
--- adOwner only
    (896, 'Ads  .1 (unpublished)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, false),
    (897, 'Ads  .2 (unpublished)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, false),
    (898, 'Ads  .3 (unpublished)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, false),
    (899, 'Ads  .4 (unpublished)', 'tester@email.com','2018-08-17 10:06:54.742','user/userIdp/adOwner', 0, false);

--- ACL Data

INSERT INTO ACL_CLASS (ID, CLASS,CLASS_ID_TYPE) VALUES
    (210, 'com.sap.cp.appsec.domain.Advertisement','');

INSERT INTO ACL_SID (ID, PRINCIPAL, SID) VALUES
    (310, true, 'user/userIdp/adOwner'),
    (311, true, 'user/userIdp/reader'), --- currently not used
    (312, false, 'ATTR:GROUP=GROUP_1'),
    (313, false, 'ATTR:GROUP=GROUP_ADMIN');

INSERT INTO ACL_OBJECT_IDENTITY (ID, OBJECT_ID_CLASS, OBJECT_ID_IDENTITY, PARENT_OBJECT, Owner_SID, ENTRIES_INHERITING) VALUES
    (410, 210, '871', NULL, 310, true),
    (411, 210, '872', NULL, 310, true),
    (412, 210, '873', NULL, 310, true),
    (413, 210, '874', NULL, 310, true),
    (414, 210, '875', NULL, 310, true),
    (415, 210, '876', NULL, 310, true),
    (416, 210, '877', NULL, 310, true),
    (417, 210, '878', NULL, 310, true),
    (418, 210, '879', NULL, 310, true),
    (419, 210, '880', NULL, 310, true),
    (420, 210, '881', NULL, 310, true),
    (421, 210, '882', NULL, 310, true),
    (422, 210, '883', NULL, 310, true),
    (423, 210, '884', NULL, 310, true),
    (424, 210, '885', NULL, 310, true),
    (425, 210, '886', NULL, 310, true),
    (426, 210, '887', NULL, 310, true),
    (427, 210, '888', NULL, 310, true),
    (428, 210, '889', NULL, 310, true),
    (429, 210, '890', NULL, 310, true),
    (430, 210, '891', NULL, 310, true),

    (440, 210, '892', NULL, 310, true),
    (441, 210, '893', NULL, 310, true),
    (442, 210, '894', NULL, 310, true),
    (443, 210, '895', NULL, 310, true),

    (460, 210, '896', NULL, 310, true),
    (461, 210, '897', NULL, 310, true),
    (462, 210, '898', NULL, 310, true),
    (463, 210, '899', NULL, 310, true);

--- read-only permissions to all ads 871..899
    -- user=adOwner
--- read-only permissions to ads with 871..891
    -- user=reader
    -- users with role attribute group=GROUP_1
--- read-only permissions to ads with 892..895
    -- users with role attribute group=GROUP_ADMIN

INSERT INTO ACL_ENTRY (ID, ACL_OBJECT_IDENTITY, ACE_ORDER, SID, MASK, GRANTING, AUDIT_SUCCESS, AUDIT_FAILURE) VALUES
    (511, 410, 1, 310, 1,   true, false, false),
    (512, 410, 2, 311, 1,   true, false, false),
    (513, 410, 3, 312, 1,   true, false, false),
    (514, 411, 1, 310, 1,   true, false, false),
    (515, 411, 2, 311, 1,   true, false, false),
    (516, 411, 3, 312, 1,   true, false, false),
    (517, 412, 1, 310, 1,   true, false, false),
    (518, 412, 2, 311, 1,   true, false, false),
    (519, 412, 3, 312, 1,   true, false, false),
    (520, 413, 1, 310, 1,   true, false, false),
    (521, 413, 2, 311, 1,   true, false, false),
    (522, 413, 3, 312, 1,   true, false, false),
    (523, 414, 1, 310, 1,   true, false, false),
    (524, 414, 2, 311, 1,   true, false, false),
    (525, 414, 3, 312, 1,   true, false, false),
    (526, 415, 1, 310, 1,   true, false, false),
    (527, 415, 2, 311, 1,   true, false, false),
    (528, 415, 3, 312, 1,   true, false, false),
    (529, 416, 1, 310, 1,   true, false, false),
    (530, 416, 2, 311, 1,   true, false, false),
    (531, 416, 3, 312, 1,   true, false, false),
    (532, 417, 1, 310, 1,   true, false, false),
    (533, 417, 2, 311, 1,   true, false, false),
    (534, 417, 3, 312, 1,   true, false, false),
    (535, 418, 1, 310, 1,   true, false, false),
    (536, 418, 2, 311, 1,   true, false, false),
    (537, 418, 3, 312, 1,   true, false, false),
    (538, 419, 1, 310, 1,   true, false, false),
    (539, 419, 2, 311, 1,   true, false, false),
    (540, 419, 3, 312, 1,   true, false, false),
    (541, 420, 1, 310, 1,   true, false, false),
    (542, 420, 2, 311, 1,   true, false, false),
    (543, 420, 3, 312, 1,   true, false, false),
    (544, 421, 1, 310, 1,   true, false, false),
    (545, 421, 2, 311, 1,   true, false, false),
    (546, 421, 3, 312, 1,   true, false, false),
    (547, 422, 1, 310, 1,   true, false, false),
    (548, 422, 2, 311, 1,   true, false, false),
    (549, 422, 3, 312, 1,   true, false, false),
    (550, 423, 1, 310, 1,   true, false, false),
    (551, 423, 2, 311, 1,   true, false, false),
    (552, 423, 3, 312, 1,   true, false, false),
    (553, 424, 1, 310, 1,   true, false, false),
    (554, 424, 2, 311, 1,   true, false, false),
    (555, 424, 3, 312, 1,   true, false, false),
    (556, 425, 1, 310, 1,   true, false, false),
    (557, 425, 2, 311, 1,   true, false, false),
    (558, 425, 3, 312, 1,   true, false, false),
    (559, 426, 1, 310, 1,   true, false, false),
    (560, 426, 2, 311, 1,   true, false, false),
    (561, 426, 3, 312, 1,   true, false, false),
    (562, 427, 1, 310, 1,   true, false, false),
    (563, 427, 2, 311, 1,   true, false, false),
    (564, 427, 3, 312, 1,   true, false, false),
    (565, 428, 1, 310, 1,   true, false, false),
    (566, 428, 2, 311, 1,   true, false, false),
    (567, 428, 3, 312, 1,   true, false, false),
    (568, 429, 1, 310, 1,   true, false, false),
    (569, 429, 2, 311, 1,   true, false, false),
    (570, 429, 3, 312, 1,   true, false, false),
    (571, 430, 1, 310, 1,   true, false, false),
    (572, 430, 2, 311, 1,   true, false, false),
    (573, 430, 3, 312, 1,   true, false, false),

    (574, 440, 1, 310, 1,   true, false, false),
    (575, 440, 2, 313, 1,   true, false, false),
    (576, 441, 1, 310, 1,   true, false, false),
    (578, 441, 2, 313, 1,   true, false, false),
    (579, 442, 1, 310, 1,   true, false, false),
    (580, 442, 2, 313, 1,   true, false, false),
    (581, 443, 1, 310, 1,   true, false, false),
    (582, 443, 2, 313, 1,   true, false, false),

    (584, 460, 1, 310, 1,   true, false, false),
    (585, 461, 1, 310, 1,   true, false, false),
    (586, 462, 1, 310, 1,   true, false, false),
    (587, 463, 1, 310, 1,   true, false, false);

                
