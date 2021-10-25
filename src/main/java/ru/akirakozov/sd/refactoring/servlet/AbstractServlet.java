package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

abstract class AbstractServlet extends HttpServlet {
    private final ProductsDao productsDao;

    protected AbstractServlet(ProductsDao productsDao) {
        this.productsDao = productsDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String html = buildHtml(request, response);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.getWriter().println(html);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String buildHtml(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SQLException, IOException;

    protected ProductsDao getProductsDao() {
        return productsDao;
    }
}
