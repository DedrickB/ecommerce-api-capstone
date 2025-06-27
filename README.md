# ecommerce-api-capstone
A RESTful API for an e-commerce platform built with Spring Boot.
# E-Commerce REST API - Final Capstone Project

This project is a fully functional backend REST API for an e-commerce platform, built with Java and the Spring Boot framework. It provides all the necessary endpoints to power a modern online store, including user management, product catalog, a persistent shopping cart, and a secure checkout process.

## Key Features

- **User Authentication:** Secure user registration and login using JWT (JSON Web Tokens).
- **Role-Based Authorization:** Endpoints for creating, updating, or deleting data are protected and restricted to ADMIN users.
- **Product Catalog:** Full CRUD (Create, Read, Update, Delete) functionality for products and categories.
- **Dynamic Product Search:** A flexible search endpoint that allows filtering products by category, price range, and color.
- **Persistent Shopping Cart:** Logged-in users have a shopping cart that saves their items in the database.
- **Secure Checkout:** A checkout process that converts a user's cart into a permanent order.

## Technologies Used

- **Java 17**
- **Spring Boot**
- **Spring Security** (for authentication and authorization)
- **Spring Data JDBC** (for database interaction)
- **MySQL** (as the relational database)
- **Maven** (for dependency management)
- **JWT (JSON Web Tokens)** (for stateless session management)

---

## API Demonstration (Postman)

The API is designed to be interacted with via HTTP requests. Here is a demonstration of the primary user workflow using Postman.

### 1. Register and Login

A new user is created and then logs in to receive a JWT authentication token.

![Register and Login](path/to/your/image1.png)
*(**Action:** Take a screenshot of your Postman window showing the Register/Login requests and responses. Save it in a folder like `docs/images` and update the path here.)*

### 2. Add Items to Shopping Cart

Using the token from login, the user adds items to their personal shopping cart.

![Add to Cart](path/to/your/image2.png)
*(**Action:** Take a screenshot showing the `POST /cart/products/{id}` request with the Bearer Token and the successful response.)*

### 3. Checkout

The user completes their purchase. The API creates an order from their cart and then clears the cart.

![Checkout](path/to/your/image3.png)
*(**Action:** Take a screenshot showing the `POST /orders` request and the created order object in the response.)*

---

## Interesting Piece of Code

One of the most interesting challenges in this project was implementing the dynamic product search feature. The goal was to create a single endpoint that could handle any combination of filters without writing a separate SQL query for each case.

The solution was to build the SQL query dynamically in the `MySqlProductsDao`.

```java
// src/main/java/org/yearup/data/mysql/MySqlProductsDao.java

@Override
public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
    // Start with a base query that is always true
    StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    // Conditionally append search criteria if they are provided
    if (categoryId != null) {
        sql.append(" AND category_id = ?");
        params.add(categoryId);
    }
    if (minPrice != null) {
        sql.append(" AND price >= ?");
        params.add(minPrice);
    }
    if (maxPrice != null) {
        sql.append(" AND price <= ?");
        params.add(maxPrice);
    }
    if (color != null && !color.trim().isEmpty()) {
        sql.append(" AND LOWER(color) LIKE LOWER(?)");
        params.add("%" + color.trim() + "%");
    }

    // Execute the dynamically built query with its parameters
    return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
}
