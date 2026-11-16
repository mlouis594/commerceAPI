package com.mlouis594.CommerceAPI.order;

import com.jayway.jsonpath.JsonPath;
import com.mlouis594.CommerceAPI.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartAndOrderIntegrationTest extends IntegrationTest {

    private void addToCart(String token, String productId, int quantity) throws Exception {
        mvc.perform(auth(withJson(post("/api/v1/cart/items"), Map.of("productId", productId, "quantity", quantity)), token))
                .andExpect(status().isOk());
    }

    private String checkout(String token) throws Exception {
        String body = mvc.perform(auth(post("/api/v1/orders/checkout"), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private void expectInventory(String productId, int expected) throws Exception {
        mvc.perform(get("/api/v1/products/" + productId)).andExpect(jsonPath("$.inventory").value(expected));
    }

    @Test
    void cartAccumulatesQuantitiesAndTotals() throws Exception {
        String token = newCustomerToken();
        String a = createProduct(unique("A"), "10.00", 50);
        String b = createProduct(unique("B"), "2.50", 50);

        addToCart(token, a, 1);
        addToCart(token, a, 2);
        addToCart(token, b, 4);

        mvc.perform(auth(get("/api/v1/cart"), token))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(7))
                .andExpect(jsonPath("$.total").value(40.0));

        mvc.perform(auth(withJson(put("/api/v1/cart/items/" + a), Map.of("quantity", 1)), token))
                .andExpect(jsonPath("$.total").value(20.0));
        mvc.perform(auth(delete("/api/v1/cart/items/" + b), token))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void cannotAddMoreThanInStock() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Rare"), "99.00", 2);

        mvc.perform(auth(withJson(post("/api/v1/cart/items"), Map.of("productId", id, "quantity", 3)), token))
                .andExpect(status().isConflict());
    }

    @Test
    void checkoutCreatesOrderDecrementsStockAndEmptiesCart() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Book"), "15.00", 10);
        addToCart(token, id, 3);

        mvc.perform(auth(post("/api/v1/orders/checkout"), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.total").value(45.0))
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        expectInventory(id, 7);
        mvc.perform(auth(get("/api/v1/cart"), token)).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(auth(get("/api/v1/orders"), token)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void orderKeepsPriceAtTimeOfPurchase() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Mug"), "8.00", 5);
        addToCart(token, id, 1);
        String orderId = checkout(token);

        mvc.perform(auth(withJson(put("/api/v1/products/" + id), Map.of("price", 12)), adminToken()));

        mvc.perform(auth(get("/api/v1/orders/" + orderId), token))
                .andExpect(jsonPath("$.items[0].unitPrice").value(8.0));
    }

    @Test
    void checkoutWithEmptyCartIs400() throws Exception {
        mvc.perform(auth(post("/api/v1/orders/checkout"), newCustomerToken())).andExpect(status().isBadRequest());
    }

    @Test
    void checkoutFailsIfStockDroppedSinceAddingToCart() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Hot"), "30.00", 5);
        addToCart(token, id, 4);
        mvc.perform(auth(withJson(put("/api/v1/products/" + id), Map.of("inventory", 2)), adminToken()));

        mvc.perform(auth(post("/api/v1/orders/checkout"), token)).andExpect(status().isConflict());
        expectInventory(id, 2); // nothing was decremented
    }

    @Test
    void customersCannotSeeEachOthersOrders() throws Exception {
        String owner = newCustomerToken();
        String id = createProduct(unique("Pen"), "1.00", 5);
        addToCart(owner, id, 1);
        String orderId = checkout(owner);

        String stranger = newCustomerToken();
        mvc.perform(auth(get("/api/v1/orders/" + orderId), stranger)).andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/v1/orders/" + orderId + "/cancel"), stranger)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/v1/orders/" + orderId), adminToken())).andExpect(status().isOk());
    }

    @Test
    void cancellingRestocksAndCannotBeRepeated() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Chair"), "40.00", 5);
        addToCart(token, id, 2);
        String orderId = checkout(token);
        expectInventory(id, 3);

        mvc.perform(auth(post("/api/v1/orders/" + orderId + "/cancel"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        expectInventory(id, 5);

        mvc.perform(auth(post("/api/v1/orders/" + orderId + "/cancel"), token)).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminsAdvanceOrderStatusThroughLifecycle() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Desk"), "100.00", 5);
        addToCart(token, id, 1);
        String orderId = checkout(token);
        String admin = adminToken();

        mvc.perform(auth(withJson(put("/api/v1/orders/" + orderId + "/status"), Map.of("status", "SHIPPED")), token))
                .andExpect(status().isForbidden());

        mvc.perform(auth(withJson(put("/api/v1/orders/" + orderId + "/status"), Map.of("status", "SHIPPED")), admin))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
        // shipped orders can no longer be cancelled
        mvc.perform(auth(post("/api/v1/orders/" + orderId + "/cancel"), token)).andExpect(status().isBadRequest());
        mvc.perform(auth(withJson(put("/api/v1/orders/" + orderId + "/status"), Map.of("status", "DELIVERED")), admin))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        mvc.perform(auth(get("/api/v1/orders/all"), token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/v1/orders/all"), admin)).andExpect(status().isOk());
    }

    @Test
    void deletingProductClearsCartsButKeepsOrderHistory() throws Exception {
        String buyer = newCustomerToken();
        String browser = newCustomerToken();
        String id = createProduct(unique("Discontinued"), "5.00", 5);
        addToCart(buyer, id, 1);
        String orderId = checkout(buyer);
        addToCart(browser, id, 1);

        mvc.perform(auth(delete("/api/v1/products/" + id), adminToken())).andExpect(status().isNoContent());

        mvc.perform(auth(get("/api/v1/cart"), browser)).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(auth(get("/api/v1/orders/" + orderId), buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(id));
    }

    @Test
    void usersWithOrderHistoryCannotBeDeleted() throws Exception {
        String token = newCustomerToken();
        String id = createProduct(unique("Kept"), "5.00", 5);
        addToCart(token, id, 1);
        checkout(token);

        mvc.perform(auth(delete("/api/v1/users/me"), token)).andExpect(status().isConflict());
    }
}
