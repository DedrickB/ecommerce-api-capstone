package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // GET http://localhost:8080/cart - Get user's cart
    @GetMapping("")
    public ShoppingCart getCart(Principal principal) {
        try {
            User user = getLoggedInUser(principal);
            return shoppingCartDao.getByUserId(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    // POST http://localhost:8080/cart/products/15 - Add a product to the cart
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(Principal principal, @PathVariable int productId) {
        try {
            User user = getLoggedInUser(principal);
            // Ensure product exists before adding
            if (productDao.getById(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }
            return shoppingCartDao.addProductToCart(user.getId(), productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    // PUT http://localhost:8080/cart/products/15 - Update quantity of a product
    @PutMapping("/products/{productId}")
    public void updateProductInCart(Principal principal, @PathVariable int productId, @RequestBody Map<String, Integer> payload) {
        try {
            User user = getLoggedInUser(principal);
            if (!payload.containsKey("quantity")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must include 'quantity'.");
            }
            int quantity = payload.get("quantity");
            shoppingCartDao.updateProductQuantity(user.getId(), productId, quantity);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    // DELETE http://localhost:8080/cart - Clear the entire cart
    @DeleteMapping("")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {
        try {
            User user = getLoggedInUser(principal);
            shoppingCartDao.clearCart(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    // Helper method to get user from the security context
    private User getLoggedInUser(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }
        return user;
    }
}