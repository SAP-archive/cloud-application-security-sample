package com.sap.cp.appsec.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.test.JwtGenerator;
import com.sap.cp.appsec.domain.AclAttribute;
import com.sap.cp.appsec.dto.AdvertisementDto;
import com.sap.cp.appsec.dto.BulletinboardDto;
import com.sap.cp.appsec.dto.PermissionDto;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

import static com.sap.cp.appsec.controllers.AdvertisementAclController.FIRST_PAGE_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdvertisementACLControllerTest {

    private static final String LOCATION = "Location";
    private static final String SOME_TITLE = "MyNewAdvertisement";

    private static final String OWNER = "adOwner";
    private static final String GROUP_MEMBER = "groupMember";
    private static final String ANYONE = "anyone";
    private static final String BOARD_VIEWER = "boardViewer";
    private static final String USER_DE = "viewer_DE";
    private static final String BOARD_OWNER = "boardAdmin";

    private static final String GROUP = "GROUP_1";
    private static final String OTHER_GROUP = "GROUP_2";
    private static final String ANOTHER_GROUP = "GROUP_3";
    private static final String ADMIN_GROUP = "GROUP_ADMIN";

    private static final String BULLETINBOARD_DE_1 = "DE_WDF03_Board";
    private static final String BULLETINBOARD_DE_2 = "DE_WDF04_Board";
    private static final String BULLETINBOARD_IL_1 = "IL_RAA03_Board";
    private static final String COUNTRY_DE = "DE";
    private static final String COUNTRY_IL = "IL";

    private String jwtToken_adsOwner;
    private String jwtToken_adsOwner_IL;
    private String jwtToken_anyone;
    private String jwtToken_userAssignedToGroup;
    private String jwtToken_userAssignedToAdminGroup;
    private String jwtToken_boardViewer_DE_1;
    private String jwtToken_boardAdmin;
    private String jwtToken_adsViewer_DE;

    @MockBean
    private AuditorAware<String> auditorAware;

    @Autowired
    private XsuaaServiceConfiguration xsuaaServiceConfiguration;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        Mockito.doAnswer(invocation -> Optional.of(OWNER)).when(auditorAware).getCurrentAuditor();

        jwtToken_adsOwner = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(OWNER)
                .addAttribute(AclAttribute.LOCATION.getXSUserAttributeName(), new String[]{COUNTRY_DE})
//                .addAttribute(AclAttribute.LOCATION.getXSUserAttributeName(), new String[]{COUNTRY_IL})
                .addAttribute(AclAttribute.BULLETINBOARD.getXSUserAttributeName(), new String[]{BULLETINBOARD_IL_1})
                .getToken().getTokenValue();

        jwtToken_adsOwner_IL = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(OWNER)
                .addAttribute(AclAttribute.LOCATION.getXSUserAttributeName(), new String[]{COUNTRY_IL})
                .getToken().getTokenValue();

        jwtToken_anyone = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(ANYONE)
                .getToken().getTokenValue();

        jwtToken_userAssignedToGroup = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(GROUP_MEMBER)
                .addAttribute(AclAttribute.GROUP.getXSUserAttributeName(), new String[]{GROUP})
                .getToken().getTokenValue();

        jwtToken_userAssignedToAdminGroup = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(GROUP_MEMBER)
                .addAttribute(AclAttribute.GROUP.getXSUserAttributeName(), new String[]{ADMIN_GROUP})
                .getToken().getTokenValue();

        jwtToken_boardViewer_DE_1 = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(BOARD_VIEWER)
                .addAttribute(AclAttribute.BULLETINBOARD.getXSUserAttributeName(), new String[]{BULLETINBOARD_DE_1})
                .getToken().getTokenValue();

        jwtToken_adsViewer_DE = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(USER_DE)
                .addAttribute(AclAttribute.LOCATION.getXSUserAttributeName(), new String[]{COUNTRY_DE})
                .getToken().getTokenValue();

        jwtToken_boardAdmin = new JwtGenerator(xsuaaServiceConfiguration.getClientId())
                .setUserName(BOARD_OWNER)
                .getToken().getTokenValue();
    }

    @Test
    public void anyone_canCreateAndReadItsAdvertisement() throws Exception {
        String id = performPostAndGetId(jwtToken_anyone);
        mockMvc.perform(buildGetRequest(id)
                .with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void anyone_canNotReadOthersAdvertisement() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);
        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authorizedUser_canReadAdvertisement() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);

        Character[] permissionCodes = new Character[]{'R'};
        mockMvc.perform(buildGrantPermissionToUserRequest(id,
                createPermissions(ANYONE, permissionCodes)).
                with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.isPublished", is(false)));
    }

    @Test
    public void authorizedUser_canNotReadAdvertisementAfterPermissionWasRemoved() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);

        Character[] permissionCodes = new Character[]{'R', 'A'};
        mockMvc.perform(buildGrantPermissionToUserRequest(id, createPermissions(ANYONE, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());
        mockMvc.perform(buildRemovePermissionToUserRequest(id, createPermissions(ANYONE, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());
        mockMvc.perform(buildGetRequest(id)
                .with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authorizedGroupMember_canReadButNotModifyAdvertisement() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest()
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        String id = getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));

        Character[] permissionCodes = new Character[]{'R'};
        mockMvc.perform(buildGrantPermissionToUserGroupRequest(id, createPermissions(GROUP, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.isPublished", is(false)));


        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = "SOME_OTHER_TITLE";
        mockMvc.perform(buildPutRequest(id, advertisement).with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void authorizedGroupMember_canModifyPublishedAdvertisement() throws Exception {
        String updatedTitle = "SOME_OTHER_TITLE";
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest()
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = updatedTitle;
        String id = getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));

        Character[] permissionCodes = new Character[]{'W'};
        mockMvc.perform(buildGrantPermissionToUserGroupRequest(id, createPermissions(GROUP, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildPutRequest(id, advertisement)
                .with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(updatedTitle)));
    }

    @Test
    public void anyGroupMember_canNotReadAdvertisementsOfOtherGroups() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);

        Character[] permissionCodes = new Character[]{'R', 'W'};
        mockMvc.perform(buildGrantPermissionToUserGroupRequest(id, createPermissions(OTHER_GROUP, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());
        mockMvc.perform(buildGrantPermissionToUserGroupRequest(id, createPermissions(ANOTHER_GROUP, permissionCodes))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql({"/db/data/acl_test_data_mass.sql"})
    public void owner_canReadItsRecentlyCreatedAdvertisements() throws Exception {
        mockMvc.perform(buildGetRequest("/my").with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value[0].id", is(899)))
                .andExpect(jsonPath("$.value[5].id", is(894)))
                .andExpect(jsonPath("$.value[19].id", is(880)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_mass.sql"})
    public void adminGroupMember_canReadAllAdvertisements() throws Exception {
        mockMvc.perform(buildGetRequest("/my").with(bearerToken(jwtToken_userAssignedToAdminGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(4)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_mass.sql"})
    public void owner_canReadAllItsAdvertisements() throws Exception {
        mockMvc.perform(buildGetRequest("/my/pages/" + FIRST_PAGE_ID).with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(20))); // see AdvertisementAclController.DEFAULT_PAGE_SIZE
        mockMvc.perform(buildGetRequest("/my/pages/" + FIRST_PAGE_ID + 1).with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(9)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_mass.sql"})
    public void groupMember_canReadAllAdvertisementsOfGroup() throws Exception {
        mockMvc.perform(buildGetRequest("/my/pages/" + FIRST_PAGE_ID)
                .with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(20))); // see AdvertisementAclController.DEFAULT_PAGE_SIZE
        mockMvc.perform(buildGetRequest("/my/pages/" + FIRST_PAGE_ID + 1)
                .with(bearerToken(jwtToken_userAssignedToGroup)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(1)));
    }

    @Test
    public void anyone_canNotPublishAdvertisementToAnyBoard() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest()
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        String id = getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));
        mockMvc.perform(buildPublishRequest(id, new BulletinboardDto(BULLETINBOARD_DE_1))
                .with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void boardViewer_canReadButNotModifyPublishedAdvertisement() throws Exception {
        String updatedTitle = "SOME_OTHER_TITLE";
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest()
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        String id = getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));

        mockMvc.perform(buildPublishRequest(id, new BulletinboardDto(BULLETINBOARD_DE_1))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildGetRequest(id)
                .with(bearerToken(jwtToken_boardViewer_DE_1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.isPublished", is(true)));

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = updatedTitle;
        mockMvc.perform(buildPutRequest(id, advertisement)
                .with(bearerToken(jwtToken_boardViewer_DE_1)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void anyOne_canNotDeleteNotExistingAdvertisement() throws Exception {
        mockMvc.perform(buildDeleteRequest("4711")
                .with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void anyOne_canNotDeleteOthersAdvertisement() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);

        mockMvc.perform(buildDeleteRequest(id)
                .with(bearerToken(jwtToken_anyone)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Ignore
    public void owner_canDeleteItsAdvertisement() throws Exception {
        String id = performPostAndGetId(jwtToken_adsOwner);

        mockMvc.perform(buildDeleteRequest(id)
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isNoContent());

        mockMvc.perform(buildGetRequest(id)
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isNotFound());
    }


    @Test
    public void boardViewer_canReadAdvertisementsPublishedOnHisBoard() throws Exception {
        for (int i = 0; i < 7; i++) {
            String id = performPostAndGetId(jwtToken_adsOwner);
            String board = (i % 2 == 0) ? BULLETINBOARD_DE_1 : BULLETINBOARD_DE_2;
            mockMvc.perform(buildPublishRequest(id, new BulletinboardDto(board))
                    .with(bearerToken(jwtToken_adsOwner)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_boardViewer_DE_1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(4)));
    }

    @Test
    public void boardAdmin_canReadAllAdvertisementsPublishedOnHisBoards() throws Exception {
        for (int i = 0; i < 7; i++) {
            String id = performPostAndGetId(jwtToken_adsOwner);
            String board = (i % 2 == 0) ? BULLETINBOARD_IL_1 : BULLETINBOARD_DE_2;
            mockMvc.perform(buildPublishRequest(id, new BulletinboardDto(board))
                    .with(bearerToken(jwtToken_adsOwner)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_boardAdmin)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(7)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_hierarchy.sql"})
    public void adsViewer_canReadAllPublishedAdvertisementsInHisLocation() throws Exception {
        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsViewer_DE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(4)));

        String id = performPostAndGetId(jwtToken_adsOwner);

        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsViewer_DE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(4))); // because it is not yet published

        mockMvc.perform(buildPublishRequest(id, new BulletinboardDto(BULLETINBOARD_DE_2))
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk());

        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsViewer_DE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(5)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_hierarchy.sql"})
    public void advertiser_canReadOnlyPublishedAdvertisementsInHisLocation() throws Exception {
        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsOwner_IL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(3)));

        performPostAndGetId(jwtToken_adsOwner_IL);

        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsOwner_IL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(3)));
    }

    @Test
    @Sql({"/db/data/acl_test_data_hierarchy.sql"})
    public void advertiser_canReadAllPublishedAdvertisementsWithoutDuplicates() throws Exception {
        mockMvc.perform(buildGetRequest("/published")
                .with(bearerToken(jwtToken_adsOwner)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value", hasSize(6))); //all except of id=307 (assigned to location=IL

    }

    private MockHttpServletRequestBuilder buildPostRequest()
            throws Exception {
        AdvertisementDto advertisement = createAdvertisement();

        return post(AdvertisementAclController.PATH).content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8);
    }

    private String performPostAndGetId(String jwtToken) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest()
                .with(bearerToken(jwtToken)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return getIdFromLocation(response.getHeader(LOCATION));
    }

    private MockHttpServletRequestBuilder buildGetRequest(String path) {
        return get(AdvertisementAclController.PATH + "/" + path);
    }

    private MockHttpServletRequestBuilder buildGrantPermissionToUserGroupRequest(String id, PermissionDto groupPermission) throws Exception {
        return put(AdvertisementAclController.PATH + "/grantPermissionsToUserGroup/" + id)
                .content(toJson(groupPermission))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildRemovePermissionToUserRequest(String id, PermissionDto userPermission) throws Exception {
        return put(AdvertisementAclController.PATH + "/removePermissionsFromUser/" + id)
                .content(toJson(userPermission))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildGrantPermissionToUserRequest(String id, PermissionDto userPermission) throws Exception {
        return put(AdvertisementAclController.PATH + "/grantPermissionsToUser/" + id)
                .content(toJson(userPermission))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildPublishRequest(String id, BulletinboardDto bulletinboard) throws Exception {
        return put(AdvertisementAclController.PATH + "/publish/" + id)
                .content(toJson(bulletinboard))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildPutRequest(String id, AdvertisementDto advertisement) throws Exception {
        return put(AdvertisementAclController.PATH + "/" + id).content(toJson(advertisement))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildDeleteRequest(String id) {
        return delete(AdvertisementAclController.PATH + "/" + id);
    }

    private String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    private Object toObject(String jsonString) throws IOException {
        return new ObjectMapper().reader().readValue(jsonString);
    }

    private String getIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String contentString = response.getContentAsString();
        return objectMapper.readValue(contentString, clazz);
    }

    private AdvertisementDto createAdvertisement() {
        AdvertisementDto newAd = new AdvertisementDto();
        newAd.title = SOME_TITLE;
        newAd.contact = "Mister X";

        return newAd;
    }

    private PermissionDto createPermissions(String name, Character[] permissionCodes) {
        PermissionDto newPermission = new PermissionDto();
        newPermission.name = name;
        newPermission.permissionCodes = permissionCodes;

        return newPermission;
    }

    private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {
        private String token;

        BearerTokenRequestPostProcessor(String token) {
            this.token = token;
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.addHeader("Authorization", "Bearer " + this.token);
            return request;
        }
    }

    private static BearerTokenRequestPostProcessor bearerToken(String token) {
        return new BearerTokenRequestPostProcessor(token);
    }

}
