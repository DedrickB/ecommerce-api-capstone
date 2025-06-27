package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlProductsDao extends MySqlDaoBase implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public MySqlProductsDao(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

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

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        String sql = "SELECT * FROM products WHERE category_id = ?;";
        return jdbcTemplate.query(sql, this::mapRow, categoryId);
    }

    @Override
    public Product getById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRow, productId);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Product create(Product product) {
        // Corrected order to match the updated mapRow method logic
        String sql = "INSERT INTO products(name, price, category_id, description, color, stock, featured, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getColor());
            ps.setInt(6, product.getStock());
            ps.setBoolean(7, product.isFeatured());
            ps.setString(8, product.getImageUrl());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            product.setProductId(keyHolder.getKey().intValue());
        }
        return product;
    }

    @Override
    public void update(int productId, Product product) {
        // Corrected order to match the updated mapRow method logic
        String sql = "UPDATE products SET " +
                " name = ?, " +
                " price = ?, " +
                " category_id = ?, " +
                " description = ?, " +
                " color = ?, " +
                " stock = ?, " +
                " featured = ?, " +
                " image_url = ? " +
                "WHERE product_id = ?;";

        jdbcTemplate.update(sql,
                product.getName(),
                product.getPrice(),
                product.getCategoryId(),
                product.getDescription(),
                product.getColor(),
                product.getStock(),
                product.isFeatured(),
                product.getImageUrl(),
                productId);
    }

    @Override
    public void delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?;";
        jdbcTemplate.update(sql, productId);
    }


    protected Product mapRow(ResultSet row, int rowNum) throws SQLException {
        Product product = new Product();
        product.setProductId(row.getInt("product_id"));
        product.setName(row.getString("name"));
        product.setPrice(row.getBigDecimal("price"));
        product.setCategoryId(row.getInt("category_id"));
        product.setDescription(row.getString("description"));
        product.setColor(row.getString("color"));
        product.setStock(row.getInt("stock"));
        product.setFeatured(row.getBoolean("featured"));
        product.setImageUrl(row.getString("image_url"));
        return product;
    }
}