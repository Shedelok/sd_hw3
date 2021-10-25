package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author akirakozov
 */
public class QueryServlet extends AbstractServlet {
    public QueryServlet(ProductsDao productsDao) {
        super(productsDao);
    }

    @Override
    protected String buildHtml(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            return getProductsDao().getProductWithMaxPrice();
        } else if ("min".equals(command)) {
            return getProductsDao().getProductWithMinPrice();
        } else if ("sum".equals(command)) {
            return getProductsDao().getSummaryPrice();
        } else if ("count".equals(command)) {
            return getProductsDao().getProductsCount();
        } else {
            return "Unknown command: " + command;
        }
    }

}
