package ru.akirakozov.sd.refactoring.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Product {
    private final String name;
    private final int price;

    public Product(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public Product(ResultSet rs) throws SQLException {
        name = rs.getString("name");
        price = rs.getInt("price");
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
