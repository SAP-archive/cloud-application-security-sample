package com.sap.cp.appsec.dto;

import com.sap.cp.appsec.domain.Advertisement;
import com.sap.cp.appsec.domain.ConfidentialityLevel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * A Data Transfer Object (DTO) is a data structure without logic.
 * <p>
 * Note: This class implements also the mapping between DTO and Entity and vice versa
 */
public class AdvertisementDto {
    /**
     * id is null in case of a new advertisement
     **/
    private Long id;

    @NotBlank
    public String title;

    @NotNull
    public BigDecimal price;

    @NotBlank
    public String contact;

    @NotBlank
    public String currency;

    public final MetaData metadata = new MetaData();

    public ConfidentialityLevel confidentialityLevel;

    /**
     * Default constructor required by Jackson JSON Converter
     */
    public AdvertisementDto() {
    }

    /**
     * Transforms Advertisement entity to DTO
     */
    public AdvertisementDto(Advertisement ad) {
        this.id = ad.getId();
        this.title = ad.getTitle();
        this.contact = ad.getContact();
        if (ad.getConfidentialityLevel() != null) {
            this.confidentialityLevel = ad.getConfidentialityLevel();
        }
        this.metadata.createdAt = convertToDateTime(ad.getCreatedAt());
        this.metadata.modifiedAt = convertToDateTime(ad.getModifiedAt());
        this.metadata.createdBy = "" + ad.getCreatedBy();
        this.metadata.modifiedBy = "" + ad.getModifiedBy();
        this.metadata.version = ad.getVersion();
    }

    // use only in tests
    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Advertisement toEntity() {
        // does not map "read-only" attributes
        Advertisement ad = new Advertisement(title, contact, confidentialityLevel);
        ad.setId(id);
        ad.setVersion(metadata.version);
        return ad;
    }

    private String convertToDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME); // ISO 8601
    }

    public static class MetaData {
        public String createdAt;
        public String modifiedAt;
        public String createdBy;
        public String modifiedBy;

        public long version = 0L;
    }
}
