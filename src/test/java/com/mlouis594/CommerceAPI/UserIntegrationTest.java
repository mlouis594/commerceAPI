package com.mlouis594.CommerceAPI;

import com.mlouis594.CommerceAPI.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTest extends IntegrationTest {

    @Test
    void userCanUpdateOwnProfileAndPassword() throws Exception {
        String username = unique("carol");
        signup(username, "oldPassword1");
        String token = login(username, "oldPassword1");

        mvc.perform(auth(withJson(put("/api/v1/users/me"), Map.of("firstName", "Caroline", "password", "newPassword1")), token))
                .andExpect(status().isOk());

        mvc.perform(auth(get("/api/v1/users/me"), token)).andExpect(jsonPath("$.firstName").value("Caroline"));
        login(username, "newPassword1");
        mvc.perform(withJson(post("/authenticate"), Map.of("username", username, "password", "oldPassword1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customersCannotUseAdminUserEndpoints() throws Exception {
        String token = newCustomerToken();
        mvc.perform(auth(get("/api/v1/users"), token)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanPromoteUser() throws Exception {
        String username = unique("dave");
        String id = signup(username, "password123");

        mvc.perform(auth(withJson(put("/api/v1/users/" + id + "/role"), Map.of("role", "ADMIN")), adminToken()))
                .andExpect(status().isOk());

        String token = login(username, "password123");
        mvc.perform(auth(get("/api/v1/users"), token)).andExpect(status().isOk());
    }

    @Test
    void userCanDeleteOwnAccountWithoutOrders() throws Exception {
        String username = unique("erin");
        signup(username, "password123");
        String token = login(username, "password123");

        mvc.perform(auth(delete("/api/v1/users/me"), token)).andExpect(status().isNoContent());
        // token now refers to a deleted user
        mvc.perform(auth(get("/api/v1/users/me"), token)).andExpect(status().isUnauthorized());
    }
}
