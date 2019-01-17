package com.sap.cp.appsec.controllers;

import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.dto.*;
import com.sap.cp.appsec.exceptions.BadRequestException;
import com.sap.cp.appsec.services.AdvertisementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(AdvertisementAclController.PATH)
@Validated
public class AdvertisementAclController {
    static final String PATH = "/api/v1/ads/acl";
    private final AdvertisementService service;

    private static final String PATH_PAGES = PATH + "/my/pages/";
    public static final int FIRST_PAGE_ID = 0;
    public static final int DEFAULT_PAGE_SIZE = 20; // allows server side optimization e.g. via caching

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    public AdvertisementAclController(AdvertisementService adsService) {
        this.service = adsService;
    }

    @PostMapping
    public ResponseEntity<AdvertisementDto> create(@RequestBody @Valid AdvertisementDto advertisement,
                                                   UriComponentsBuilder uriComponentsBuilder) {
        Advertisement savedAds = service.create(advertisement.toEntity());

        UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}")
                .buildAndExpand(savedAds.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(new AdvertisementDto(savedAds), headers, HttpStatus.CREATED);
    }

    @GetMapping("/hello-token")
    public Map<String, String> message(@AuthenticationPrincipal Token token) {
        Map<String, String> result = new HashMap<>();
        result.put("grant type", token.getGrantType());
        result.put("client id", token.getClientId());
        result.put("tenant id", token.getSubaccountId());
        result.put("logon name", token.getLogonName());
        result.put("family name", token.getFamilyName());
        result.put("given name", token.getGivenName());
        result.put("email", token.getEmail());
        result.put("token", token.getAppToken());
        result.put("authorizations", token.getAuthorities().toString());

        return result;
    }

    @PutMapping("/{id}")
    public AdvertisementDto update(@RequestBody AdvertisementDto updatedAdvertisement, @PathVariable("id") Long id) {
        throwIfInconsistent(id, updatedAdvertisement.getId());

        Advertisement updatedAds = service.update(updatedAdvertisement.toEntity());
        logger.trace("updated ad with version {}", updatedAdvertisement.metadata.version);
        return new AdvertisementDto(updatedAds);
    }

    @GetMapping("/{id}")
    public AdvertisementDto read(@PathVariable("id") @Min(0) Long id) {
        AdvertisementDto advertisement = new AdvertisementDto(service.findById(id));
        logger.trace("returning: {}", advertisement);
        return advertisement;
    }

    /**
     * Read all my advertisements, I'm directly authorized to as owner, delegate, admin.
     */
    @GetMapping("/my")
    public ResponseEntity<AdvertisementListDto> readAll() {
        return readMyAdvertisementsPage(FIRST_PAGE_ID, DEFAULT_PAGE_SIZE);
    }

    @GetMapping("/my/pages/{pageId}")
    public ResponseEntity<AdvertisementListDto> readPage(@PathVariable("pageId") int pageId) {
        return readMyAdvertisementsPage(pageId, DEFAULT_PAGE_SIZE);
    }

    @PutMapping("/grantPermissionsToUser/{id}")
    public void grantPermissionsToUser(@PathVariable("id") @Min(0) Long id, @RequestBody PermissionDto userPermission) {
        service.grantPermissions(id, userPermission.name, userPermission.getPermissions());
    }

    @PutMapping("/removePermissionsFromUser/{id}")
    public void removePermissionsFromUser(@PathVariable("id") @Min(0) Long id, @RequestBody PermissionDto userPermission) {
        service.removePermissions(id, userPermission.name, userPermission.getPermissions());
    }

    @PutMapping("/grantPermissionsToUserGroup/{id}")
    public void grantPermissionsToUserGroup(@PathVariable("id") @Min(0) Long id, @RequestBody PermissionDto groupPermission) {
        service.grantPermissionsToUserGroup(id, groupPermission.name, groupPermission.getPermissions());
    }


    /**
     * Publishes advertisement to a board,
     * so that all that have access to board or to all boards in location can read it.
     */
    @PutMapping("/publish/{id}")
    public void publishToBoard(@PathVariable("id") @Min(0) Long id, @RequestBody BulletinboardDto bulletinboard) {
        service.publishToBulletinboard(id, bulletinboard.name);
    }

    /**
     * Read all published advertisements, I have directly (owner, delegate)
     * and indirectly read access rights to (attribute: location, board,...).
     */
    @GetMapping("/published")
    public ResponseEntity<AdvertisementListDto> readAllPublished() {
        return readPublishedAdvertisementsPage(FIRST_PAGE_ID, DEFAULT_PAGE_SIZE);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    private ResponseEntity<AdvertisementListDto> readMyAdvertisementsPage(int pageId, int pageSize) {
        Page page = service.findAll(pageId, pageSize, Sort.Direction.DESC, new String[]{"ads.id"});

        return new ResponseEntity<>(new AdvertisementListDto(page.getContent()),
                PageHeaderBuilder.createLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    private ResponseEntity<AdvertisementListDto> readPublishedAdvertisementsPage(int pageId, int pageSize) {
        Page page = service.findAllPublished(pageId, pageSize, Sort.Direction.DESC, new String[]{"ads.id"});

        return new ResponseEntity<>(new AdvertisementListDto(page.getContent()),
                PageHeaderBuilder.createLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    private void throwIfInconsistent(Long expected, Long actual) {
        if (!expected.equals(actual)) {
            String message = String.format(
                    "bad request, inconsistent IDs between request and object: request id = %d, object id = %d",
                    expected, actual);
            throw new BadRequestException(message);
        }
    }

}
