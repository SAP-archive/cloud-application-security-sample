package com.sap.cp.appsec.domain;

import com.sap.cp.appsec.controllers.AttributeFinderController;

public enum AclAttribute {

    GROUP(AttributeFinderController.ATTRIBUTE_GROUP),
    BULLETINBOARD(AttributeFinderController.ATTRIBUTE_BULLETINBOARD),
    LOCATION(AttributeFinderController.ATTRIBUTE_LOCATION);

    private String attributeName;

    AclAttribute(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getXSUserAttributeName() {
        return this.attributeName;
    }

    public String getSidForAttributeValue(String value) {
        return this.getSidPrefix() + value;
    }

    public String getSidPrefix() {
        return "ATTR:" + this.attributeName.toUpperCase() + "=";
    }

    public String getAttributeName() {
        return this.attributeName;
    }
}
