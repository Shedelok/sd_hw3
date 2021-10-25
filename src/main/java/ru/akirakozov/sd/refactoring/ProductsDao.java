package ru.akirakozov.sd.refactoring;

import ru.akirakozov.sd.refactoring.entity.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductsDao {
    private final String databaseUrl;

    public ProductsDao(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void createDatabaseIfNotExist() throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " PRICE          INT     NOT NULL)";
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    public void insertProduct(String name, long price) throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            String sql = "INSERT INTO PRODUCT " +
                    "(NAME, PRICE) VALUES (\"" + name + "\"," + price + ")";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> result = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");

            while (rs.next()) {
                result.add(new Product(rs.getString("name"), rs.getInt("price")));
            }

            rs.close();
            stmt.close();
        }

        return result;
    }

    public Optional<Product> getProductWithMaxPrice() throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1")) {
                    if (rs.next()) {
                        return Optional.of(new Product(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    public Optional<Product> getProductWithMinPrice() throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1")) {
                    if (rs.next()) {
                        return Optional.of(new Product(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    public Optional<Integer> getSummaryPrice() throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT SUM(price) FROM PRODUCT")) {
                    if (rs.next()) {
                        return Optional.of(rs.getInt(1));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    public Optional<Integer> getProductsCount() throws SQLException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            try (Statement stmt = c.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT")) {
                    if (rs.next()) {
                        return Optional.of(rs.getInt(1));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }
}
