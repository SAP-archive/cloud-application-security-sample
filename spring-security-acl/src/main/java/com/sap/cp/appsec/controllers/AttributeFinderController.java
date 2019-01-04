package com.sap.cp.appsec.controllers;

import com.sap.cp.appsec.domain.AclAttribute;
import com.sap.cp.appsec.security.AclSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Validated
@RequestMapping(AttributeFinderController.PATH)
public class AttributeFinderController {

    static final String PATH = "/api/v1/attribute";
    public static final String ATTRIBUTE_GROUP = "group";
    public static final String ATTRIBUTE_BULLETINBOARD = "bulletinboard";
    public static final String ATTRIBUTE_LOCATION = "location";

    private final AclSupport aclSupport;

    public AttributeFinderController(AclSupport aclSupport) {
        this.aclSupport = aclSupport;
    }

    @GetMapping("/{ATTRIBUTE_NAME}")
    public List<String> getAllValuesForAttribute(@PathVariable("ATTRIBUTE_NAME") String attributeName) {
        List<String> attributeValues = new ArrayList<>();
        switch (attributeName) {
            case ATTRIBUTE_GROUP:
                attributeValues = getAclAttributeValues(AclAttribute.GROUP);
                break;
            case ATTRIBUTE_BULLETINBOARD:
                attributeValues = getAclAttributeValues(AclAttribute.BULLETINBOARD);
                break;
            case ATTRIBUTE_LOCATION:
                attributeValues = getAclAttributeValues(AclAttribute.LOCATION);
                break;
            default:
                break;
        }
        return attributeValues;
    }

    private List<String> getAclAttributeValues(AclAttribute aclAttribute) {
        List<String> attributeValues = new ArrayList<>();

        for (String sid : aclSupport.getAllSidsWithPrefix(aclAttribute.getSidPrefix())) {
            String attributeValue = sid.replace(aclAttribute.getSidPrefix(), "");
            attributeValues.add(attributeValue);
        }
        return attributeValues;
    }

    @GetMapping
    public List<String> getAllAttributes() {
        List<String> attributes = new ArrayList<>();
        attributes.add(ATTRIBUTE_GROUP);
        attributes.add(ATTRIBUTE_BULLETINBOARD);
        attributes.add(ATTRIBUTE_LOCATION);
        return attributes;
    }

}
