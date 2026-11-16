package com.mlouis594.CommerceAPI;

import com.mlouis594.CommerceAPI.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductIntegrationTest extends IntegrationTest {

    @Test
    void catalogueIsPublic() throws Exception {
        String id = createProduct(unique("Widget"), "9.99", 10);

        mvc.perform(get("/api/v1/products")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.inventory").value(10));
    }

    @Test
    void customersCannotModifyProducts() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Gadget"), "5.00", 1);

        mvc.perform(auth(withJson(post("/api/v1/products"), Map.of("name", "X", "price", 1, "inventory", 1)), token))
                .andExpect(status().isForbidden());
        mvc.perform(auth(withJson(put("/api/v1/products/" + id), Map.of("price", 0)), token))
                .andExpect(status().isForbidden());
        mvc.perform(auth(delete("/api/v1/products/" + id), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void partialUpdateOnlyChangesProvidedFields() throws Exception {
        String name = unique("Lamp");
        String id = createProduct(name, "20.00", 7);

        mvc.perform(auth(withJson(put("/api/v1/products/" + id), Map.of("price", 25)), adminToken()))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/products/" + id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.price").value(25.0))
                .andExpect(jsonPath("$.inventory").value(7));
    }

    @Test
    void adminCanDeleteProduct() throws Exception {
        String id = createProduct(unique("Temp"), "1.00", 1);

        mvc.perform(auth(delete("/api/v1/products/" + id), adminToken())).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void invalidProductIsRejected() throws Exception {
        mvc.perform(auth(withJson(post("/api/v1/products"), Map.of("name", "", "price", -1, "inventory", -5)), adminToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.inventory").exists());
    }

    @Test
    void unknownAndMalformedIds() throws Exception {
        mvc.perform(get("/api/v1/products/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/products/not-a-uuid")).andExpect(status().isBadRequest());
    }
}
