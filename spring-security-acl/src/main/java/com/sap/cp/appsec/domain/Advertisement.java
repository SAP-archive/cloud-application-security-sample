package com.sap.cp.appsec.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
    @Column(name = "is_published")
    private boolean isPublished;

    /**
     * Any JPA Entity needs a default constructor.
     */
    public Advertisement() {
    }

    public Advertisement(String title, String contact) {
        this.title = title;
        this.contact = contact;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContact() {
        return contact;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    @Override
    public String toString() {
        return "Advertisement [id=" + id + ", title=" + title + "]";
    }
}
