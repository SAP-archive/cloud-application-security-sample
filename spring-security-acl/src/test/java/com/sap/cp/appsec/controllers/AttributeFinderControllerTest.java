package com.sap.cp.appsec.controllers;

import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AttributeFinderControllerTest {
    private static final String ACL_SID_INSERT_STMT = "INSERT INTO ACL_SID (ID, PRINCIPAL, SID) VALUES " +
            "(90000, true, 'owner')," +
            "(90001, false, 'ATTR:GROUP=GROUP')," +
            "(90002, false, 'ATTR:GROUP_=GROUP')," +
            "(90003, false, 'ATTR:GROUP_:GROUP')," +
            "(90004, false, 'ATTR:GROUP=ADMIN');";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAll() throws Exception {
        // check that the returned location is correct
        mockMvc.perform(get(AttributeFinderController.PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    @Sql(statements = ACL_SID_INSERT_STMT)
    public void createAndGetByAclAttribute() throws Exception {
        // check that the returned location is correct
        mockMvc.perform(get(AttributeFinderController.PATH + "/" + AttributeFinderController.ATTRIBUTE_GROUP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(JSONArray.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$", hasItem("GROUP")))
                .andExpect(jsonPath("$", hasItem("ADMIN")));
    }
}
