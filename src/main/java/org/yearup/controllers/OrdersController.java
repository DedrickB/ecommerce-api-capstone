package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.*;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController {

    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrdersController(OrderDao orderDao, ShoppingCartDao shoppingCartDao, UserDao userDao, ProfileDao profileDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // FIX #2: Check if the profile exists before using it
            Profile profile = profileDao.getByUserId(userId);
            if (profile == null || profile.getAddress() == null || profile.getAddress().isEmpty()) {
                // Throw a specific error if the user has no shipping address
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile is incomplete. Please add a shipping address before checking out.");
            }

            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart.");
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setDate(LocalDateTime.now());
            // Now it's safe to get the address
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setShippingAmount(new BigDecimal("0.00"));

            List<OrderLineItem> lineItems = new ArrayList<>();
            for (ShoppingCartItem cartItem : cart.getItems().values()) {
                OrderLineItem lineItem = new OrderLineItem();
                lineItem.setProductId(cartItem.getProduct().getProductId());
                lineItem.setQuantity(cartItem.getQuantity());
                lineItem.setSalesPrice(cartItem.getProduct().getPrice());
                lineItem.setDiscount(cartItem.getDiscountPercent());
                lineItems.add(lineItem);
            }
            order.setItems(lineItems);

            Order createdOrder = orderDao.create(order);
            shoppingCartDao.clearCart(userId);

            return createdOrder;

        } catch (Exception ex) {
            // Re-throw specific known exceptions, otherwise wrap as a generic internal error
            if (ex instanceof ResponseStatusException) {
                throw ex;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }
}