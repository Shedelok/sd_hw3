package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends AbstractServlet {
    public GetProductsServlet(ProductsDao productsDao) {
        super(productsDao);
    }

    @Override
    protected String buildHtml(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        return getProductsDao().getAllProducts();
    }
}
