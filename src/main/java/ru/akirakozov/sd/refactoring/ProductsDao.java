package ru.akirakozov.sd.refactoring;

import javax.servlet.http.HttpServletResponse;
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

    public void getAllProducts(HttpServletResponse response) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");
            response.getWriter().println("<html><body>");

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                response.getWriter().println(name + "\t" + price + "</br>");
            }
            response.getWriter().println("</body></html>");

            rs.close();
            stmt.close();
        }
    }

    public void getProductWithMaxPrice(HttpServletResponse response) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h1>Product with max price: </h1>");

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                response.getWriter().println(name + "\t" + price + "</br>");
            }
            response.getWriter().println("</body></html>");

            rs.close();
            stmt.close();
        }
    }

    public void getProductWithMinPrice(HttpServletResponse response) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h1>Product with min price: </h1>");

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                response.getWriter().println(name + "\t" + price + "</br>");
            }
            response.getWriter().println("</body></html>");

            rs.close();
            stmt.close();
        }
    }

    public void getSummaryPrice(HttpServletResponse response) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(price) FROM PRODUCT");
            response.getWriter().println("<html><body>");
            response.getWriter().println("Summary price: ");

            if (rs.next()) {
                response.getWriter().println(rs.getInt(1));
            }
            response.getWriter().println("</body></html>");

            rs.close();
            stmt.close();
        }
    }

    public void getProductsCount(HttpServletResponse response) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(databaseUrl)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT");
            response.getWriter().println("<html><body>");
            response.getWriter().println("Number of products: ");

            if (rs.next()) {
                response.getWriter().println(rs.getInt(1));
            }
            response.getWriter().println("</body></html>");

            rs.close();
            stmt.close();
        }
    }
}
