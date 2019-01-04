package com.sap.cp.appsec.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BulletinboardDto {

    @NotBlank
    @NotNull
    public String name;

    /**
     * Default constructor required by Jackson JSON Converter
     */
    public BulletinboardDto() {
    }

    public BulletinboardDto(String name) {
        this.name = name;
    }
}
