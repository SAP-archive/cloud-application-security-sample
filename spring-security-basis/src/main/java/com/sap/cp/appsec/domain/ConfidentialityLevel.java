package com.sap.cp.appsec.domain;

import java.util.ArrayList;
import java.util.List;

public enum ConfidentialityLevel {
    /**
     * Please note: ordinal is relevant in AdvertisementSpecificationBuilder
     **/
    PUBLIC("Public"),  // ordinal 0
    INTERNAL("Internal"),
    CONFIDENTIAL("Confidential"),
    STRICTLY_CONFIDENTIAL("Strictly confidential");

    public static final String ATTRIBUTE_NAME = "confidentiality_level";

    private String description;
    private int level = super.ordinal();

    ConfidentialityLevel(String description) {
        this.description = description;
    }

    public int getLevel() {
        return this.level;
    }

    public String getDescription() {
        return this.description;
    }

    public static List<String> getValues() {
        ConfidentialityLevel[] values = ConfidentialityLevel.values();
        ArrayList<String> stringValues = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            stringValues.add(values[i].toString());
        }
        return stringValues;
    }
}
