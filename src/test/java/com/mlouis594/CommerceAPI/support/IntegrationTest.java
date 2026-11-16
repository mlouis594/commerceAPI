package com.mlouis594.CommerceAPI.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for full-stack tests: real HTTP layer (MockMvc), real security,
 * real Postgres in a container. The Spring context, and so the container, is
 * shared across all subclasses. Tests create their own uniquely named data so
 * they stay independent of each other and of the seeded demo data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin12345";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    protected static String unique(String prefix) {
        return prefix + "_" + COUNTER.incrementAndGet() + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    protected String toJson(Object body) throws Exception {
        return json.writeValueAsString(body);
    }

    protected MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON).content(toJson(body));
    }

    protected static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected String login(String username, String password) throws Exception {
        String body = mvc.perform(withJson(post("/authenticate"), Map.of("username", username, "password", password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    protected String signup(String username, String password) throws Exception {
        String body = mvc.perform(withJson(post("/api/v1/users/signup"), Map.of(
                        "username", username, "firstName", "Test", "lastName", "User", "password", password)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readValue(body, String.class);
    }

    /** Signs up a fresh customer and returns their token. */
    protected String newCustomerToken() throws Exception {
        String username = unique("customer");
        signup(username, "password123");
        return login(username, "password123");
    }

    /** Creates a product as admin and returns its id. */
    protected String createProduct(String name, String price, int inventory) throws Exception {
        String body = mvc.perform(auth(withJson(post("/api/v1/products"), Map.of(
                        "name", name, "price", new BigDecimal(price), "inventory", inventory)), adminToken()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readValue(body, String.class);
    }
}
