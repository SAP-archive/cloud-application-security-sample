package com.sap.cp.appsec.services;

import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.security.AclSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Sql({"/db/data/acl_test_data.sql"})
public class AdvertisementServiceTest {

    @Autowired
    private AdvertisementService service;

    @Autowired
    private AclSupport aclService;

    private Long advertisementId = 777L;

    private static final String OWNER = "ownerAndAdmin";
    private static final String ADMIN = "noOwnerButAdmin";
    private static final String READER = "noOwnerButReader";
    private static final String WRITER = "noOwnerButWriter";
    private static final String ANYONE = "anyone";
    private static final String OTHERUSER = "otherUser";

    @MockBean
    private AuditorAware<String> auditorAware;


    @Before
    public void setUp() {
        Mockito.doAnswer(invocation -> {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(user.getUsername());
        }).when(auditorAware).getCurrentAuditor();
    }

    @Test
    @WithMockUser(username = ANYONE) //can be realized w/o ACL
    public void anyOne_canCreateAndRead() {
        String title = "my new advertisement";
        Advertisement adsCreated = service.create(buildAdvertisement(title));

        assertThat(adsCreated, is(notNullValue()));
        assertThat(adsCreated.getTitle(), is(title));

        Advertisement adsRead = service.findById(adsCreated.getId());
        assertThat(adsRead.getTitle(), is(title));
    }

    @Test
    @WithMockUser(username = OWNER) //can be realized w/o ACL
    public void owner_canRead() {
        Advertisement adsRead = service.findById(advertisementId);
        assertThat(adsRead, is(notNullValue()));
    }

    @Test
    @WithMockUser(username = OWNER) //can be realized w/o ACL
    public void owner_canModify() {
        String newTitle = "Ads updated";
        Advertisement adsCreatedByOwner = service.findById(advertisementId);
        adsCreatedByOwner.setTitle(newTitle);

        Advertisement adsUpdated = service.update(adsCreatedByOwner);
        assertThat(adsUpdated.getTitle(), is(newTitle));
    }

    @Test
    @WithMockUser(username = OWNER)
    public void owner_canGrantAdminPermissionsToOtherUser() {
        service.grantPermissions(advertisementId, OTHERUSER, new Permission[]{BasePermission.ADMINISTRATION});
        boolean otherUserIsAdmin = hasUserPermission(OTHERUSER, new Permission[]{BasePermission.ADMINISTRATION});
        assertThat(otherUserIsAdmin, is(true));
    }

    @Test
    @WithMockUser(username = READER)
    public void reader_canRead() {
        boolean readerHasReadPermission = hasUserPermission(READER, new Permission[]{BasePermission.READ});
        assertThat(readerHasReadPermission, is(true));

        Advertisement adsRead = service.findById(advertisementId);
        assertThat(adsRead, is(notNullValue()));
    }

    @WithMockUser(username = OWNER)
    public void owner_canRemoveReadPermission() {
        service.removePermissions(advertisementId, READER, new Permission[]{BasePermission.READ});

        boolean readerHasReadPermission = hasUserPermission(READER, new Permission[]{BasePermission.READ});
        assertThat(readerHasReadPermission, is(false));
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = READER)
    public void reader_cannotModify() {
        boolean readerHasWritePermission = hasUserPermission(READER, new Permission[]{BasePermission.WRITE});
        assertThat(readerHasWritePermission, is(false));

        Advertisement adsCreatedByOwner = service.findById(advertisementId);
        adsCreatedByOwner.setTitle("Ads updated");
        service.update(adsCreatedByOwner);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = READER)
    public void reader_cannotGrantPermissions() {
        service.grantPermissions(advertisementId, OTHERUSER, new Permission[]{BasePermission.READ});
    }

    @Test
    @WithMockUser(username = ADMIN)
    public void admin_canGrantReadWritePermissions() {
        service.grantPermissions(advertisementId, OTHERUSER, new Permission[]{BasePermission.READ, BasePermission.WRITE});

        boolean otherUserCanReadAndWrite = hasUserPermission(OTHERUSER, new Permission[]{BasePermission.READ, BasePermission.WRITE});
        assertThat(otherUserCanReadAndWrite, is(true));
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = WRITER)
    public void writer_cannotGrantAdminPermissions() {
        service.grantPermissions(advertisementId, OTHERUSER, new Permission[]{BasePermission.ADMINISTRATION});
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = WRITER)
    public void writer_cannotDelete() {
        service.deleteById(advertisementId);
    }

    @Test
    @WithMockUser(username = OWNER)
    @Ignore
    public void owner_canDelete() {
        assertThat(service.findById(advertisementId), notNullValue());
        service.deleteById(advertisementId);
        assertThat(service.findById(advertisementId), nullValue());
        assertThat(hasUserPermission(OWNER, new Permission[]{BasePermission.ADMINISTRATION}), is(false));
    }

    @Test
    @WithMockUser(username = ADMIN)
    @Ignore
    public void admin_canDelete() {
        service.deleteById(advertisementId);
        assertThat(hasUserPermission(ADMIN, new Permission[]{BasePermission.ADMINISTRATION}), is(false));
    }

    private boolean hasUserPermission(String principal, Permission[] permissions) {
        return aclService.hasUserPermission(Advertisement.class.getName(), advertisementId, principal, permissions);
    }

    private Advertisement buildAdvertisement(String title) {
        return new Advertisement(title, "tester@test.com");
    }
}

