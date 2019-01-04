package com.sap.cp.appsec.dto;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;

public class PageHeaderBuilder {

    public static HttpHeaders createLinkHeader(Page<?> page, String path) {
        StringBuilder linkHeader = new StringBuilder();
        if (page.hasPrevious()) {
            int prevNumber = page.getNumber() - 1;
            linkHeader.append("<").append(path).append(prevNumber).append(">; rel=\"previous\"");
            if (!page.isLast()) {
                linkHeader.append(", ");
            }
        }
        if (page.hasNext()) {
            int nextNumber = page.getNumber() + 1;
            linkHeader.append("<").append(path).append(nextNumber).append(">; rel=\"next\"");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK, linkHeader.toString());
        return headers;
    }
}