package com.sap.cp.appsec.controllers;

import com.sap.cp.appsec.domain.ConfidentialityLevel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(AttributeFinder.PATH)
public class AttributeFinder {

    public static final String ATTRIBUTE_CONFIDENTIALITY_LEVEL = ConfidentialityLevel.ATTRIBUTE_NAME;
    static final String PATH = "/api/v1/attribute";

    @GetMapping("/{ATTRIBUTE_NAME}")
    public List<String> getAllValuesForAttribute(@PathVariable("ATTRIBUTE_NAME") String attributeName) {
        switch (attributeName) {
            case ATTRIBUTE_CONFIDENTIALITY_LEVEL:
                return ConfidentialityLevel.getValues();
            default:
                return Collections.emptyList();
        }
    }

    @GetMapping
    public List<String> getAllAttributes() {
        List<String> attributes = new ArrayList<>();
        attributes.add(ATTRIBUTE_CONFIDENTIALITY_LEVEL);
        return attributes;
    }

}
