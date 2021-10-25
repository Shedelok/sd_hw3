package ru.akirakozov.sd.refactoring;

import java.io.IOException;
import java.sql.*;

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

    public String getAllProducts() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder("<html><body>").append(System.lineSeparator());
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                resultBuilder.append(name).append('\t').append(price).append("</br>").append(System.lineSeparator());
            }
            resultBuilder.append("</body></html>");

            rs.close();
            stmt.close();
        }

        return resultBuilder.toString();
    }

    public String getProductWithMaxPrice() throws SQLException, IOException {
        StringBuilder resultBuilder = new StringBuilder("<html><body>").append(System.lineSeparator());

        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1");
            resultBuilder.append("<h1>Product with max price: </h1>").append(System.lineSeparator());

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                resultBuilder.append(name).append("\t").append(price).append("</br>").append(System.lineSeparator());
            }
            resultBuilder.append("</body></html>");

            rs.close();
            stmt.close();
        }

        return resultBuilder.toString();
    }

    public String getProductWithMinPrice() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder("<html><body>").append(System.lineSeparator());
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1");
            resultBuilder.append("<h1>Product with min price: </h1>").append(System.lineSeparator());

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                resultBuilder.append(name).append("\t").append(price).append("</br>").append(System.lineSeparator());
            }
            resultBuilder.append("</body></html>");

            rs.close();
            stmt.close();
        }

        return resultBuilder.toString();
    }

    public String getSummaryPrice() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder("<html><body>").append(System.lineSeparator());
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(price) FROM PRODUCT");
            resultBuilder.append("Summary price: ").append(System.lineSeparator());

            if (rs.next()) {
                resultBuilder.append(rs.getInt(1)).append(System.lineSeparator());
            }
            resultBuilder.append("</body></html>");

            rs.close();
            stmt.close();
        }

        return resultBuilder.toString();
    }

    public String getProductsCount() throws SQLException {
        StringBuilder resultBuilder = new StringBuilder("<html><body>").append(System.lineSeparator());
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT");
            resultBuilder.append("Number of products: ").append(System.lineSeparator());

            if (rs.next()) {
                resultBuilder.append(rs.getInt(1)).append(System.lineSeparator());
            }
            resultBuilder.append("</body></html>");

            rs.close();
            stmt.close();
        }
        return resultBuilder.toString();
    }
}
