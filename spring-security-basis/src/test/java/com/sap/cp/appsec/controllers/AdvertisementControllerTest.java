package com.sap.cp.appsec.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.test.JwtGenerator;
import com.sap.cp.appsec.domain.ConfidentialityLevel;
import com.sap.cp.appsec.dto.AdvertisementDto;
import org.junit.Before;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sap.cp.appsec.controllers.AdvertisementController.DEFAULT_PAGE_SIZE;
import static com.sap.cp.appsec.controllers.AdvertisementController.FIRST_PAGE_ID;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdvertisementControllerTest {

    private static final String SOME_TITLE = "MyNewAdvertisement";
    private static final String SOME_OTHER_TITLE = "MyOldAdvertisement";

    private String jwt_advertiser;
    private String jwt_adsViewer_public;
    private String jwt_otherAdvertiser;
    private static final String OWNER = "owner";
    private static final String SOME_OTHER_PERSON = "other advertiser";
    private static final String VIEWER = "viewer";

    @MockBean
    AuditorAware<String> auditorAware;

    @Autowired
    private XsuaaServiceConfiguration xsuaaServiceConfiguration;

    @Before
    public void setUp() {
        Mockito.doAnswer(invocation -> {
            return Optional.of(OWNER);
        }).when(auditorAware).getCurrentAuditor();

        // compute valid token with Display and Update scopes
        jwt_advertiser = new JwtGenerator()
                .setUserName(OWNER)
                .addScopes(getGlobalScope("Display"), getGlobalScope("Update"))
                .getTokenForAuthorizationHeader();
        jwt_adsViewer_public = new JwtGenerator()
                .setUserName(VIEWER)
                .addScopes(getGlobalScope("Display"))
                .addAttribute(AttributeFinder.ATTRIBUTE_CONFIDENTIALITY_LEVEL, new String[]{ConfidentialityLevel.PUBLIC.name()})
                .getTokenForAuthorizationHeader();
        jwt_otherAdvertiser = new JwtGenerator()
                .setUserName(SOME_OTHER_PERSON).addScopes(getGlobalScope("Display"), getGlobalScope("Update"))
                .getTokenForAuthorizationHeader();
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void create_201() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, is(not(""))))
                .andExpect(jsonPath("$.title", is(SOME_TITLE)))
                .andExpect(jsonPath("$.confidentialityLevel", is(ConfidentialityLevel.STRICTLY_CONFIDENTIAL.toString())))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.title", is(SOME_TITLE))); // requires
        // com.jayway.jsonpath:json-path
    }

    @Test
    public void createNullTitle_400() throws Exception {
        mockMvc.perform(buildPostRequest(null).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    public void createBlancTitle_400() throws Exception {
        mockMvc.perform(buildPostRequest("").with(bearerToken(jwt_advertiser)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    public void createWithNoContent_400() throws Exception {
        mockMvc.perform(post(AdvertisementController.PATH).contentType(APPLICATION_JSON_UTF8)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    public void createNotAuthorized_403() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE, null)
                .with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getByLocation_200() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // check that the returned location is correct
        mockMvc.perform(get(response.getHeader(HttpHeaders.LOCATION)).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void readByIdNotAuthorized_403() throws Exception {
        mockMvc.perform(buildGetRequest("4711")
                .with(bearerToken(new JwtGenerator().addScopes(getGlobalScope("Update"))
                        .getTokenForAuthorizationHeader())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void readByIdNotFound_404() throws Exception {
        mockMvc.perform(buildGetRequest("4711").with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    public void readByIdWithNegativeId_400() throws Exception {
        mockMvc.perform(buildGetRequest("-1").with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    public void readByIdMyAd_200() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void readByIdPublic_200() throws Exception {
        String location = mockMvc.perform(buildPostRequest("DON'T see me - as I'm internal", ConfidentialityLevel.INTERNAL)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        mockMvc.perform(buildGetRequest(getIdFromLocation(location)).with(bearerToken(jwt_adsViewer_public))) // read as viewer
                .andExpect(status().isNotFound());

        location = mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.PUBLIC)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        mockMvc.perform(buildGetRequest(getIdFromLocation(location)).with(bearerToken(jwt_adsViewer_public))) // read as viewer
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE)));
    }

    @Test
    public void readAllMyAds_200() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.CONFIDENTIAL).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated());

        mockMvc.perform(buildGetRequest("/pages/" + FIRST_PAGE_ID).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(2)));
    }

    @Test
    public void readAllMyAdsAndPublic_200() throws Exception {
        mockMvc.perform(buildPostRequest("DON'T see me - as I'm confidential", ConfidentialityLevel.CONFIDENTIAL)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest("DON'T see me - as I'm internal", ConfidentialityLevel.INTERNAL)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest("See me - as I'm public", ConfidentialityLevel.PUBLIC)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());

        Mockito.doAnswer(invocation -> {
            return Optional.of(SOME_OTHER_PERSON);
        }).when(auditorAware).getCurrentAuditor();

        mockMvc.perform(buildPostRequest("See me - as I belong to you", ConfidentialityLevel.CONFIDENTIAL)
                .with(bearerToken(jwt_otherAdvertiser))).andExpect(status().isCreated());

        // should not read the ads from the other advertiser
        mockMvc.perform(buildGetRequest("/pages/" + FIRST_PAGE_ID).with(bearerToken(jwt_otherAdvertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(2)));
    }

    @Test
    public void readByConfidentiality() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.STRICTLY_CONFIDENTIAL)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.CONFIDENTIAL)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.INTERNAL)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());
        mockMvc.perform(buildPostRequest(SOME_TITLE, ConfidentialityLevel.PUBLIC)
                .with(bearerToken(jwt_advertiser))).andExpect(status().isCreated());

        mockMvc.perform(get(AdvertisementController.PATH + "/confidentiality/" + ConfidentialityLevel.PUBLIC.name())
                .with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(1))) // only public is expected
                .andExpect(jsonPath("$.value[0].confidentialityLevel", is(ConfidentialityLevel.PUBLIC.name())));

        mockMvc.perform(get(AdvertisementController.PATH + "/confidentiality/" + ConfidentialityLevel.INTERNAL.name())
                .with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isForbidden()); // adsViewer only allowed to view public ads

        mockMvc.perform(get(AdvertisementController.PATH + "/confidentiality/" + ConfidentialityLevel.PUBLIC.name())
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isForbidden()); // advertiser has no attributes -> not allowed to view public ads
    }

    @Test
    public void updateById_200() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = SOME_OTHER_TITLE;
        String id = getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));

        mockMvc.perform(buildPutRequest(id, advertisement)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_OTHER_TITLE)));
    }

    @Test
    public void updateByNotMatchingId_400() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.setId(1188L);

        mockMvc.perform(buildPutRequest(getIdFromLocation(response.getHeader(HttpHeaders.LOCATION)), advertisement)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateOthers_403() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
        advertisement.title = SOME_OTHER_TITLE;

        mockMvc.perform(buildPutRequest(getIdFromLocation(response.getHeader(HttpHeaders.LOCATION)), advertisement)
                .with(bearerToken(jwt_otherAdvertiser)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteOthers_403() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id).with(bearerToken(jwt_otherAdvertiser)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteById_204() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isNoContent());

        mockMvc.perform(buildGetRequest(id).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteByIdNotAuthorized_403() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id).with(bearerToken(jwt_adsViewer_public)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void doNotReuseIdsOfDeletedItems() throws Exception {
        String id = performPostAndGetId();

        mockMvc.perform(buildDeleteRequest(id).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isNoContent());

        String idNewAd = performPostAndGetId();

        assertThat(idNewAd, is(not(id)));
    }

    @Test
    public void readFailsWhenUnauthenticated_401() throws Exception {
        mockMvc.perform(buildGetRequest("anyId")).andExpect(status().isUnauthorized());
    }

    @Test
    public void readAdsFromSeveralPages() throws Exception {
        int adsCount = DEFAULT_PAGE_SIZE + 1;

        for (int i = 0; i < adsCount; i++) {
            performPostAndGetId();
        }

        mockMvc.perform(buildGetByPageRequest(0).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(DEFAULT_PAGE_SIZE)));

        mockMvc.perform(buildGetByPageRequest(1).with(bearerToken(jwt_advertiser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value.length()", is(1)));
    }

    @Test
    public void navigatePages() throws Exception {
        int adsCount = (DEFAULT_PAGE_SIZE * 2) + 1;

        for (int i = 0; i < adsCount; i++) {
            performPostAndGetId();
        }

        // get query
        String linkHeader = performGetRequest(AdvertisementController.PATH).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader, is("</api/v1/ads/pages/1>; rel=\"next\""));

        // navigate to next
        String nextLink = extractLinks(linkHeader).get(0);
        String linkHeader2ndPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader2ndPage,
                is("</api/v1/ads/pages/0>; rel=\"previous\", </api/v1/ads/pages/2>; rel=\"next\""));

        // navigate to next
        nextLink = extractLinks(linkHeader2ndPage).get(1);
        String linkHeader3rdPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
        assertThat(linkHeader3rdPage, is("</api/v1/ads/pages/1>; rel=\"previous\""));

        // navigate to previous
        String previousLink = extractLinks(linkHeader3rdPage).get(0);
        assertThat(performGetRequest(previousLink).getHeader(HttpHeaders.LINK), is(linkHeader2ndPage));
    }

    private String getGlobalScope(String localScope) {
        Assert.hasText(xsuaaServiceConfiguration.getAppId(), "make sure that xsuaa.xsappname is configured properly.");
        return xsuaaServiceConfiguration.getAppId() + "." + localScope;
    }

    private MockHttpServletResponse performGetRequest(String path) throws Exception {
        return mockMvc.perform(get(path).with(bearerToken(jwt_advertiser))) // can read all its own only
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
    }

    private MockHttpServletRequestBuilder buildGetByPageRequest(int pageId) {
        return get(AdvertisementController.PATH_PAGES + pageId);
    }

    private MockHttpServletRequestBuilder buildPostRequest(String adsTitle) throws Exception {
        return buildPostRequest(adsTitle, null);
    }

    private MockHttpServletRequestBuilder buildPostRequest(String adsTitle, ConfidentialityLevel level)
            throws Exception {
        AdvertisementDto advertisement = createAdvertisement(adsTitle, level);

        // post the advertisement as a JSON entity in the request body
        return post(AdvertisementController.PATH).content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8);
    }

    private String performPostAndGetId() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)
                .with(bearerToken(jwt_advertiser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return getIdFromLocation(response.getHeader(HttpHeaders.LOCATION));
    }

    private MockHttpServletRequestBuilder buildGetRequest(String id) throws Exception {
        return buildGetRequest(id, null);
    }

    private MockHttpServletRequestBuilder buildGetRequest(String id, String category) {
        return get(AdvertisementController.PATH + "/" + id + (category == null ? "" : ("?category=" + category)));
    }

    private MockHttpServletRequestBuilder buildPutRequest(String id, AdvertisementDto advertisement) throws Exception {
        return put(AdvertisementController.PATH + "/" + id).content(toJson(advertisement))
                .contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildDeleteRequest(String id) {
        return delete(AdvertisementController.PATH + "/" + id);
    }

    private static List<String> extractLinks(final String linkHeader) {
        final List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<(?<link>\\S+)>");
        final Matcher matcher = pattern.matcher(linkHeader);
        while (matcher.find()) {
            links.add(matcher.group("link"));
        }
        return links;
    }

    private String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    private String getIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String contentString = response.getContentAsString();
        return objectMapper.readValue(contentString, clazz);
    }

    private AdvertisementDto createAdvertisement(String title) {
        AdvertisementDto newAd = new AdvertisementDto();
        newAd.title = title;
        newAd.contact = "Mister X";
        newAd.currency = "EUR";
        newAd.price = new BigDecimal(42.24);

        return newAd;
    }

    private AdvertisementDto createAdvertisement(String title, ConfidentialityLevel confidentiality) {
        AdvertisementDto newAd = createAdvertisement(title);
        newAd.confidentialityLevel = confidentiality; // if null. DEFAULT in backend: STRICTLY_CONFIDENTIAL
        return newAd;
    }

    private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {
        private String token;

        public BearerTokenRequestPostProcessor(String token) {
            this.token = token;
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.addHeader(HttpHeaders.AUTHORIZATION, this.token);
            return request;
        }
    }

    private static BearerTokenRequestPostProcessor bearerToken(String token) {
        return new BearerTokenRequestPostProcessor(token);
    }
}
