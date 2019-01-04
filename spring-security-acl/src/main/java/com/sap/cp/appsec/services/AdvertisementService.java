package com.sap.cp.appsec.services;

import com.sap.cloud.security.xsuaa.token.TokenImpl;
import com.sap.cp.appsec.domain.AclAttribute;
import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.domain.AdvertisementAclRepository;
import com.sap.cp.appsec.exceptions.NotFoundException;
import com.sap.cp.appsec.security.AclSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import javax.validation.constraints.Min;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class AdvertisementService {

    private final AdvertisementAclRepository repository;

    private final AclSupport aclService;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    public AdvertisementService(AdvertisementAclRepository repository,
                                AclSupport aclService) {
        this.repository = repository;
        this.aclService = aclService;
    }

    @Transactional
    public Advertisement create(Advertisement newAds) {
        Advertisement savedAds = repository.save(newAds);

        String userName = getUniqueCurrentUserName();

        aclService.grantPermissionsToUser(
                Advertisement.class.getName(),
                savedAds.getId(),
                userName,
                new Permission[]{BasePermission.READ, BasePermission.WRITE, BasePermission.ADMINISTRATION}); //TODO Admin only is not enough

        logger.debug("successfully granted permissions (16,2,1) to the advertisement owner: " + userName);

        return savedAds;
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'read')")
    public Advertisement findById(Long id) throws NotFoundException {
        Optional<Advertisement> advertisement = repository.findById(id);
        if (!advertisement.isPresent()) {
            NotFoundException notFoundException = new NotFoundException("no Advertisement with id " + id);
            logger.warn("request failed", notFoundException);
            throw notFoundException;
        }
        return advertisement.get();
    }

    public Page<Advertisement> findAll(int pageNumber, int pageSize, Sort.Direction sortDirection, String[] properties) throws NotFoundException {
        MySidRetrievalStrategyImpl sidRetrievalStrategy = new MySidRetrievalStrategyImpl();
        Set<String> sids = sidRetrievalStrategy.getGrantedAuthorities(SecurityContextHolder.getContext().getAuthentication());
        sids.add(sidRetrievalStrategy.getPrincipalSid(SecurityContextHolder.getContext().getAuthentication()));

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, new Sort(sortDirection, properties));

        return repository.findAllByPermission(BasePermission.READ.getMask(), sids.toArray(new String[0]), pageRequest);
    }

    public Page<Advertisement> findAllPublished(int pageNumber, int pageSize, Sort.Direction sortDirection, String[] properties) throws NotFoundException {
        MySidRetrievalStrategyImpl sidRetrievalStrategy = new MySidRetrievalStrategyImpl();
        Set<String> sids = sidRetrievalStrategy.getGrantedAuthorities(SecurityContextHolder.getContext().getAuthentication());
        sids.add(sidRetrievalStrategy.getPrincipalSid(SecurityContextHolder.getContext().getAuthentication()));

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, new Sort(sortDirection, properties));

        return repository.findAllPublishedByHierarchicalPermission(BasePermission.READ.getMask(), sids.toArray(new String[0]), pageRequest);
    }

    @PreAuthorize("hasPermission(#updatedAds, 'write')")
    public Advertisement update(Advertisement updatedAds) {
        assert repository.existsById(updatedAds.getId());

        return repository.save(updatedAds);
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')")
    @Transactional
    public void grantPermissions(Long id, String principal, Permission[] permissions) {

        aclService.grantPermissionsToUser(
                Advertisement.class.getName(),
                id,
                getUniqueUserName(principal),
                permissions);
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')")
    @Transactional
    public void removePermissions(Long id, String principal, Permission[] permissions) {
        aclService.removePermissionFromUser(
                Advertisement.class.getName(),
                id,
                getUniqueUserName(principal),
                permissions);
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')")
    @Transactional
    public void grantPermissionsToUserGroup(Long id, String groupName, Permission[] permissions) {
        aclService.grantPermissionsToSid(
                Advertisement.class.getName(),
                id,
                AclAttribute.GROUP.getSidForAttributeValue(groupName),
                permissions);
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')"
            + " and hasPermission(#boardName, 'bulletinboard', 'read')")
    @Transactional
    public void publishToBulletinboard(@PathVariable("id") @Min(0) Long id, String boardName) {

        Advertisement ads = findById(id);

        aclService.setParent(
                Advertisement.class.getName(),
                ads.getId(),
                AclAttribute.BULLETINBOARD.getAttributeName(),
                boardName);

        ads.setPublished(true);
        repository.save(ads);
    }

    @PreAuthorize("hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')")
    @Transactional
    public void deleteById(Long id) {
    }


    private String getUniqueCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return new PrincipalSid(auth).getPrincipal();
    }

    private String getUniqueUserName(String userName) {
        Object currentUsersPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String origin = currentUsersPrincipal instanceof TokenImpl ? ((TokenImpl) currentUsersPrincipal).getOrigin() : null;
        return origin == null ? userName : TokenImpl.getUniquePrincipalName(origin, userName);
    }

    // TODO: contribute?? maybe this can be implemented by SidRetrievalStrategyImpl
    private class MySidRetrievalStrategyImpl extends SidRetrievalStrategyImpl {

        Set<String> getGrantedAuthorities(Authentication authentication) {
            List<Sid> sids = super.getSids(authentication);

            Set<String> grantedAuthorities = new HashSet<>();
            for (Sid sid : sids) {
                if (sid instanceof GrantedAuthoritySid) {
                    grantedAuthorities.add(((GrantedAuthoritySid) sid).getGrantedAuthority());
                }
            }
            return grantedAuthorities;
        }

        String getPrincipalSid(Authentication authentication) {
            List<Sid> sids = super.getSids(authentication);

            for (Sid sid : sids) {
                if (sid instanceof PrincipalSid) {
                    return ((PrincipalSid) sid).getPrincipal();
                }
            }
            logger.error("Unexpected error: there is no Principal Sid for user");
            return null; //Should never happen
        }
    }
}
