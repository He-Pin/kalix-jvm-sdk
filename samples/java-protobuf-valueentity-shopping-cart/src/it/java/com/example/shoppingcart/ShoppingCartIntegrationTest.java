/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.shoppingcart;

import com.example.shoppingcart.*;
import io.grpc.StatusRuntimeException;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import com.google.protobuf.Empty;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class ShoppingCartIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
          new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final ShoppingCartService client;
  private final ShoppingCartAction actionClient;

  public ShoppingCartIntegrationTest() {
    client = testKit.getGrpcClient(ShoppingCartService.class);
    actionClient = testKit.getGrpcClient(ShoppingCartAction.class);
  }

  ShoppingCartApi.Cart getCart(String cartId) throws Exception {
    return client
        .getCart(ShoppingCartApi.GetShoppingCart.newBuilder().setCartId(cartId).build())
        .toCompletableFuture()
        .get();
  }

  void addItem(String cartId, String productId, String name, int quantity) throws Exception {
    client
        .addItem(
            ShoppingCartApi.AddLineItem.newBuilder()
                .setCartId(cartId)
                .setProductId(productId)
                .setName(name)
                .setQuantity(quantity)
                .build())
        .toCompletableFuture()
        .get();
  }

  void removeItem(String cartId, String productId) throws Exception {
    client
        .removeItem(
            ShoppingCartApi.RemoveLineItem.newBuilder()
                .setCartId(cartId)
                .setProductId(productId)
                .build())
        .toCompletableFuture()
        .get();
  }

  void removeCart(String cartId, String userRole) throws Exception {
    ((ShoppingCartActionClient) actionClient)
        .removeCart().addHeader("UserRole", userRole)
        .invoke(ShoppingCartApi.RemoveShoppingCart.newBuilder().setCartId(cartId).build())
        .toCompletableFuture()
        .get();
  }

  ShoppingCartApi.LineItem item(String productId, String name, int quantity) {
    return ShoppingCartApi.LineItem.newBuilder()
        .setProductId(productId)
        .setName(name)
        .setQuantity(quantity)
        .build();
  }

  ShoppingCartController.NewCartCreated initializeCart() throws Exception {
    return actionClient.initializeCart(ShoppingCartController.NewCart.getDefaultInstance())
        .toCompletableFuture()
        .get();
  }

  ShoppingCartController.NewCartCreated createPrePopulated() throws Exception {
    return actionClient.createPrePopulated(ShoppingCartController.NewCart.getDefaultInstance())
        .toCompletableFuture()
        .get();
  }

  Empty verifiedAddItem(ShoppingCartApi.AddLineItem in) throws Exception {
    return actionClient.verifiedAddItem(in)
        .toCompletableFuture()
        .get();
  }

  @Test
  public void emptyCartByDefault() throws Exception {
    assertEquals("shopping cart should be empty", 0, getCart("user1").getItemsCount());
  }

  @Test
  public void addItemsToCart() throws Exception {
    addItem("cart2", "a", "Apple", 1);
    addItem("cart2", "b", "Banana", 2);
    addItem("cart2", "c", "Cantaloupe", 3);
    ShoppingCartApi.Cart cart = getCart("cart2");
    assertEquals("shopping cart should have 3 items", 3, cart.getItemsCount());
    assertEquals(
        "shopping cart should have expected items",
        cart.getItemsList(),
        List.of(item("a", "Apple", 1), item("b", "Banana", 2), item("c", "Cantaloupe", 3)));
  }

  @Test
  public void removeItemsFromCart() throws Exception {
    addItem("cart3", "a", "Apple", 1);
    addItem("cart3", "b", "Banana", 2);
    ShoppingCartApi.Cart cart1 = getCart("cart3");
    assertEquals("shopping cart should have 2 items", 2, cart1.getItemsCount());
    assertEquals(
        "shopping cart should have expected items",
        cart1.getItemsList(),
        List.of(item("a", "Apple", 1), item("b", "Banana", 2)));
    removeItem("cart3", "a");
    ShoppingCartApi.Cart cart2 = getCart("cart3");
    assertEquals("shopping cart should have 1 item", 1, cart2.getItemsCount());
    assertEquals(
        "shopping cart should have expected items",
        cart2.getItemsList(),
        List.of(item("b", "Banana", 2)));
  }

  @Test
  public void removeCart() throws Exception {
    String cartId = "cart4";
    addItem(cartId, "a", "Apple", 42);
    ShoppingCartApi.Cart cart1 = getCart(cartId);
    assertEquals("shopping cart should have 1 item", 1, cart1.getItemsCount());
    assertEquals(
        "shopping cart should have expected items",
        cart1.getItemsList(),
        List.of(item("a", "Apple", 42)));
    removeCart(cartId, "Admin");
    assertEquals("shopping cart should be empty", 0, getCart(cartId).getItemsCount());
  }

  @Test(expected = ExecutionException.class)
  public void notRemoveCartWithoutAdminRole() throws Exception {
    String cartId = "cart5";
    addItem(cartId, "a", "Apple", 42);
    ShoppingCartApi.Cart cart1 = getCart(cartId);
    assertEquals("shopping cart should have 1 item", 1, cart1.getItemsCount());
    assertEquals(
        "shopping cart should have expected items",
        cart1.getItemsList(),
        List.of(item("a", "Apple", 42)));
    removeCart(cartId, "StandardUser");
    assertEquals("shopping cart should have 1 item", 1, cart1.getItemsCount());
  }

  @Test
  public void createNewCart() throws Exception {
    ShoppingCartController.NewCartCreated newCartCreated = initializeCart();
    String cartId = newCartCreated.getCartId();

    ShoppingCartApi.Cart cart = getCart(cartId);
    assertTrue(cart.getCreationTimestamp() > 0L);
  }

  @Test
  public void createNewPrePopulatedCart() throws Exception {
    ShoppingCartController.NewCartCreated newCartCreated = createPrePopulated();
    String cartId = newCartCreated.getCartId();

    ShoppingCartApi.Cart cart = getCart(cartId);
    assertTrue(cart.getCreationTimestamp() > 0L);
    assertEquals(1, cart.getItemsCount());
  }

  @Test
  public void verifiedAddItem() throws Exception {
    final String cartId = "carrot-cart";
    assertThrows(Exception.class, () ->
      verifiedAddItem(ShoppingCartApi.AddLineItem.newBuilder()
          .setCartId(cartId)
          .setProductId("c")
          .setName("Carrot")
          .setQuantity(4)
          .build())
    );
    verifiedAddItem(ShoppingCartApi.AddLineItem.newBuilder()
        .setCartId(cartId)
        .setProductId("b")
        .setName("Banana")
        .setQuantity(1)
        .build());
    ShoppingCartApi.Cart cart = getCart(cartId);
    assertEquals(1, cart.getItemsCount());
  }

}
