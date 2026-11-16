package com.mlouis594.CommerceAPI;

import com.mlouis594.CommerceAPI.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    void signupThenLoginReturnsUsableToken() throws Exception {
        String username = unique("alice");
        signup(username, "s3cretPass!");
        String token = login(username, "s3cretPass!");

        mvc.perform(auth(get("/api/v1/users/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mvc.perform(withJson(post("/authenticate"), Map.of("username", ADMIN_USERNAME, "password", "nope")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void unknownUserIs401() throws Exception {
        mvc.perform(withJson(post("/authenticate"), Map.of("username", "ghost", "password", "whatever1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithGarbageTokenIs401() throws Exception {
        mvc.perform(auth(get("/api/v1/cart"), "not.a.jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateUsernameIs409() throws Exception {
        String username = unique("bob");
        signup(username, "password123");

        mvc.perform(withJson(post("/api/v1/users/signup"), Map.of(
                        "username", username, "firstName", "B", "lastName", "B", "password", "password123")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidSignupReportsFieldErrors() throws Exception {
        mvc.perform(withJson(post("/api/v1/users/signup"), Map.of(
                        "username", "has space", "firstName", "", "lastName", "X", "password", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }
}
