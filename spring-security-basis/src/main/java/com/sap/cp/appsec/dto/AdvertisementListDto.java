package com.sap.cp.appsec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.cp.appsec.domain.Advertisement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AdvertisementListDto {
    @JsonProperty("value")
    public List<AdvertisementDto> advertisements;

    public AdvertisementListDto(Iterable<Advertisement> ads) {
        this.advertisements = StreamSupport.stream(ads.spliterator(), false).map(AdvertisementDto::new)
                .collect(Collectors.toList());
    }
}