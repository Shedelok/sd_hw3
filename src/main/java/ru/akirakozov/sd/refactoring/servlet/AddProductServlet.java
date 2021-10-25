package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * @author akirakozov
 */
public class AddProductServlet extends AbstractServlet {
    public AddProductServlet(ProductsDao productsDao) {
        super(productsDao);
    }

    @Override
    protected String buildHtml(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        String name = request.getParameter("name");
        long price = Long.parseLong(request.getParameter("price"));

        getProductsDao().insertProduct(name, price);

        return "OK";
    }
}
