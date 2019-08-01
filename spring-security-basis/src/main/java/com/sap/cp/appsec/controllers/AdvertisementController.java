package com.sap.cp.appsec.controllers;

import static com.sap.cp.appsec.security.AdvertisementSpecificationBuilder.confidentialityIsEqualOrLess;
import static com.sap.cp.appsec.security.AdvertisementSpecificationBuilder.hasId;
import static com.sap.cp.appsec.security.AdvertisementSpecificationBuilder.isCreatedBy;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import java.util.Optional;

import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.domain.AdvertisementRepository;
import com.sap.cp.appsec.domain.ConfidentialityLevel;
import com.sap.cp.appsec.dto.AdvertisementDto;
import com.sap.cp.appsec.dto.AdvertisementListDto;
import com.sap.cp.appsec.dto.PageHeaderBuilder;
import com.sap.cp.appsec.exceptions.BadRequestException;
import com.sap.cp.appsec.exceptions.NotAuthorizedException;
import com.sap.cp.appsec.exceptions.NotFoundException;
import com.sap.xs2.security.container.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RequestScope
@RestController
@Validated
/*
 * Use a path which does not end with a slash! Otherwise the controller is not reachable when not using the trailing
 * slash in the URL
 */
@RequestMapping(AdvertisementController.PATH)
public class AdvertisementController {
    static final String PATH = "/api/v1/ads";
    private final AdvertisementRepository adsRepo;

    public static final String PATH_PAGES = PATH + "/pages/";
    public static final int FIRST_PAGE_ID = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AdvertisementController(AdvertisementRepository adsRepo) {
        this.adsRepo = adsRepo;
    }

    @PostMapping
    public ResponseEntity<AdvertisementDto> create(@RequestBody @Valid AdvertisementDto advertisement,
            UriComponentsBuilder uriComponentsBuilder) {

        AdvertisementDto savedAdvertisement = new AdvertisementDto(adsRepo.save(advertisement.toEntity()));
        logger.trace("created ad with version {}", savedAdvertisement.metadata.version);
        UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}")
                .buildAndExpand(savedAdvertisement.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(savedAdvertisement, headers, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<AdvertisementListDto> readAll(@AuthenticationPrincipal Token token) {
        if (!token.getAuthorities().contains(new SimpleGrantedAuthority("Display"))) {
            throw new NotAuthorizedException("This operation requires \"Display\" scope");
        }

        return readPage(FIRST_PAGE_ID);
    }

    @GetMapping("/confidentiality/{confidentialityLevel}")
    @PreAuthorize("hasAuthority('Display') and @webSecurity.hasAttributeValue('confidentiality_level', #confidentialityLevel)")
    public ResponseEntity<AdvertisementListDto> readByConfidentiality(
            @PathVariable("confidentialityLevel") String confidentialityLevel) {
        Page<Advertisement> page = adsRepo
                .findAllByConfidentialityLevel(ConfidentialityLevel.valueOf(confidentialityLevel),
                        PageRequest.of(FIRST_PAGE_ID, DEFAULT_PAGE_SIZE));

        return new ResponseEntity<>(new AdvertisementListDto(page.getContent()),
                PageHeaderBuilder.createLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    @GetMapping("/pages/{pageId}")
    public ResponseEntity<AdvertisementListDto> readPage(@PathVariable("pageId") int pageId) {
        Token jwtToken = SpringSecurityContext.getToken();
        Page<Advertisement> page = adsRepo
                .findAll(where(isCreatedBy(jwtToken.getLogonName()).or(confidentialityIsEqualOrLess(
                        jwtToken.getXSUserAttribute(ConfidentialityLevel.ATTRIBUTE_NAME)))),
                        PageRequest.of(pageId, DEFAULT_PAGE_SIZE));

        return new ResponseEntity<>(new AdvertisementListDto(page.getContent()),
                PageHeaderBuilder.createLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public AdvertisementDto readById(@PathVariable("id") @Min(0) Long id) {
        MDC.put("endpoint", "GET: " + PATH + "/" + id);
        Token jwtToken = SpringSecurityContext.getToken();

        // here we apply a filter on database leveraging Spring Data JPA: isCreatedBy or hasAttributeValue
        // find further info here: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
        Optional<Advertisement> advertisement = adsRepo
                .findOne(where(hasId(id).and(isCreatedBy(jwtToken.getLogonName())).or(
                        confidentialityIsEqualOrLess(
                                jwtToken.getXSUserAttribute(ConfidentialityLevel.ATTRIBUTE_NAME)))));

        if (advertisement.isPresent()) {
            logger.trace("returning: {}", advertisement.get());
            return new AdvertisementDto(advertisement.get());
        }
        throwNonexisting(id);
        return null;
    }

    @PutMapping("/{id}")
    @PreAuthorize("@webSecurity.isCreatedBy(#id)")
    public AdvertisementDto update(@RequestBody AdvertisementDto updatedAdvertisement, @PathVariable("id") Long id) {
        throwIfInconsistent(id, updatedAdvertisement.getId());
        throwIfNonexisting(id);
        logger.trace("updated ad with version {}", updatedAdvertisement.metadata.version);
        return new AdvertisementDto(adsRepo.save(updatedAdvertisement.toEntity()));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAuthority('Update') and @webSecurity.isCreatedBy(#id)")
    public void deleteById(@PathVariable("id") Long id) {
        throwIfNonexisting(id);
        adsRepo.deleteById(id);
    }

    private void throwIfNonexisting(@PathVariable("id") Long id) {
        if (!adsRepo.existsById(id)) {
            throwNonexisting(id);
        }
    }

    private void throwNonexisting(@PathVariable("id") Long id) {
        NotFoundException notFoundException = new NotFoundException("no Advertisement with id " + id);
        logger.warn("request failed", notFoundException);
        throw notFoundException;
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
