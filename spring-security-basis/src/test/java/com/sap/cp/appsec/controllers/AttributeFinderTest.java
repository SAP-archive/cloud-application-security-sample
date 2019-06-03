package com.sap.cp.appsec.controllers;

import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AttributeFinderTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAll() throws Exception {
        // check that the returned location is correct
        mockMvc.perform(get(AttributeFinder.PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    public void getByConfidentiality() throws Exception {
        // check that the returned location is correct
        mockMvc.perform(get(AttributeFinder.PATH + "/" + AttributeFinder.ATTRIBUTE_CONFIDENTIALITY_LEVEL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", is(both(greaterThan(0)).and(lessThan(10)))))
                .andExpect(jsonPath("$", hasItem("STRICTLY_CONFIDENTIAL")));
    }

}
