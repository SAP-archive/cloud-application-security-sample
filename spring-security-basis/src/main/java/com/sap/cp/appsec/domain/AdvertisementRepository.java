package com.sap.cp.appsec.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdvertisementRepository extends PagingAndSortingRepository<Advertisement, Long> , JpaSpecificationExecutor<Advertisement> {
    List<Advertisement> findByTitle(String title);
    
    Page<Advertisement> findAllByConfidentialityLevel(ConfidentialityLevel confidentialityLevel, Pageable pageable);

    boolean existsByIdAndCreatedBy(Long id, String owner);
}