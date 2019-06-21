package com.sap.cp.appsec.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "advertisement")
public class Advertisement extends BaseEntity {

    /**
     * mandatory fields
     **/
    @NotBlank
    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "contact")
    private String contact;

    @NotNull
    @Column(name = "confidentiality_level")
    private ConfidentialityLevel confidentialityLevel;

    @Transient
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Any JPA Entity needs a default constructor.
     */
    public Advertisement() {
    }

    public Advertisement(String title, String contact, ConfidentialityLevel confidentialityLevel) {
        this.title = title;
        this.contact = contact;
        if (confidentialityLevel != null) {
            this.confidentialityLevel = confidentialityLevel;
        } else {
            this.confidentialityLevel = ConfidentialityLevel.STRICTLY_CONFIDENTIAL;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ConfidentialityLevel getConfidentialityLevel() {
        return confidentialityLevel;
    }

    public String getContact() {
        return contact;
    }

    @Override
    public String toString() {
        return "Advertisement [id=" + id + ", title=" + title + "]";
    }
}
