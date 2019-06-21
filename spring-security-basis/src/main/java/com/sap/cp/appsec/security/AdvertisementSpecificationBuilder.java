package com.sap.cp.appsec.security;

import javax.persistence.criteria.Path;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.domain.ConfidentialityLevel;
import org.springframework.data.jpa.domain.Specification;

public class AdvertisementSpecificationBuilder {

    private AdvertisementSpecificationBuilder() {
    }

    public static Specification<Advertisement> isCreatedBy(final String createdBy) {
        return (root, critQuery, critBuilder) -> {
            Path<Object> createdByPath = root.get("createdBy");
            return critBuilder.equal(createdByPath, createdBy);
        };
    }

    public static Specification<Advertisement> confidentialityIsEqualOrLess(String[] confidentialityLevels) {
        Optional<ConfidentialityLevel> maxConfidentialityLevel = confidentialityLevels != null ? Stream.of(confidentialityLevels)
                .map(ConfidentialityLevel::valueOf)
                .max(Comparator.comparing(ConfidentialityLevel::getLevel)) : Optional.of(ConfidentialityLevel.PUBLIC);

        return (root, critQuery, critBuilder) -> {
            return critBuilder.lessThanOrEqualTo(root.get("confidentialityLevel"),
                    maxConfidentialityLevel.get()); // compares enum ordinals
        };
    }

    public static Specification<Advertisement> hasId(Long id) {
        return (root, critQuery, critBuilder) -> {
            return critBuilder.equal(root.get("id"), id);
        };
    }
}
