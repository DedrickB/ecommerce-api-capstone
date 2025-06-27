package org.yearup.data.mysql;

import org.springframework.stereotype.Repository;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.springframework.jdbc.core.JdbcTemplate; // Make sure this import is present

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    // FIX 1: Add the jdbcTemplate field
    private final JdbcTemplate jdbcTemplate;

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
        // FIX 2: Initialize the jdbcTemplate in the constructor
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        String sql = "SELECT p.*, sc.quantity, sc.user_id " + // Added user_id for clarity
                "FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE sc.user_id = ?;";

        ShoppingCart cart = new ShoppingCart();

        // FIX 3: Use 'this.jdbcTemplate' instead of 'getJdbcTemplate()'
        this.jdbcTemplate.query(sql, (rs, rowNum) -> {
            ShoppingCartItem item = mapRowToItem(rs);
            cart.add(item);
            return item;
        }, userId);

        return cart;
    }

    @Override
    public ShoppingCart addProductToCart(int userId, int productId) {
        // Check if the item is already in the cart
        String checkSql = "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        int count = this.jdbcTemplate.queryForObject(checkSql, Integer.class, userId, productId);

        if (count > 0) {
            // If it exists, increment the quantity
            String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?;";
            this.jdbcTemplate.update(updateSql, userId, productId);
        } else {
            // If it's a new item, insert it with quantity 1
            String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1);";
            this.jdbcTemplate.update(insertSql, userId, productId);
        }
        return getByUserId(userId);
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            // If quantity is 0 or less, remove the item from the cart
            String deleteSql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?;";
            this.jdbcTemplate.update(deleteSql, userId, productId);
        } else {
            // Otherwise, update the quantity
            String updateSql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";
            this.jdbcTemplate.update(updateSql, quantity, userId, productId);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";
        this.jdbcTemplate.update(sql, userId);
    }

    private ShoppingCartItem mapRowToItem(ResultSet rs) throws SQLException {
        // Create the product from the result set.
        // We need to create a temporary products DAO to use its mapRow method.
        MySqlProductsDao productsDao = new MySqlProductsDao(this.jdbcTemplate.getDataSource());
        Product product = productsDao.mapRow(rs, 0);

        // Create the shopping cart item and set its properties
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(rs.getInt("quantity"));

        return item;
    }
}