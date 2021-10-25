package ru.akirakozov.sd.refactoring;

import ru.akirakozov.sd.refactoring.entity.Product;
import ru.akirakozov.sd.refactoring.util.ThrowingFunction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductsDao {
    private final String databaseUrl;

    private static <R> Optional<R> fromFirstRow(
            ResultSet rs,
            ThrowingFunction<ResultSet, R, SQLException> function
    ) throws SQLException {
        if (rs.next()) {
            return Optional.of(function.apply(rs));
        } else {
            return Optional.empty();
        }
    }

    public ProductsDao(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    private <R> R executingQuery(
            String sql,
            ThrowingFunction<ResultSet, R, SQLException> function
    ) throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    return function.apply(rs);
                }
            }
        }
    }

    public void createDatabaseIfNotExist() throws SQLException {
        executeUpdate("CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)");
    }

    public void insertProduct(String name, long price) throws SQLException {
        executeUpdate("INSERT INTO PRODUCT " +
                "(NAME, PRICE) VALUES (\"" + name + "\"," + price + ")");
    }

    public List<Product> getAllProducts() throws SQLException {
        return executingQuery("SELECT * FROM PRODUCT", rs -> {
            List<Product> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Product(rs.getString("name"), rs.getInt("price")));
            }
            return result;
        });
    }

    public Optional<Product> getProductWithMaxPrice() throws SQLException {
        return executingQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1", rs -> fromFirstRow(rs, Product::new));
    }

    public Optional<Product> getProductWithMinPrice() throws SQLException {
        return executingQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1", rs -> fromFirstRow(rs, Product::new));
    }

    public Optional<Integer> getSummaryPrice() throws SQLException {
        return executingQuery("SELECT SUM(price) FROM PRODUCT", rs -> fromFirstRow(rs, it -> it.getInt(1)));
    }

    public Optional<Integer> getProductsCount() throws SQLException {
        return executingQuery("SELECT COUNT(*) FROM PRODUCT", rs -> fromFirstRow(rs, it -> it.getInt(1)));
    }
}
