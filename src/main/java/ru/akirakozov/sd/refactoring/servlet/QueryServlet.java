package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.HtmlBuildingUtils;
import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author akirakozov
 */
public class QueryServlet extends AbstractServlet {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> List<T> optionalToList(Optional<T> optional) {
        return optional
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    public QueryServlet(ProductsDao productsDao) {
        super(productsDao);
    }

    @Override
    protected String buildHtml(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            return HtmlBuildingUtils.buildListOfProducts(
                    Optional.of("Product with max price: "),
                    optionalToList(getProductsDao().getProductWithMaxPrice())
            );
        } else if ("min".equals(command)) {
            return HtmlBuildingUtils.buildListOfProducts(
                    Optional.of("Product with min price: "),
                    optionalToList(getProductsDao().getProductWithMinPrice())
            );
        } else if ("sum".equals(command)) {
            return HtmlBuildingUtils.buildStats("Summary price: ", getProductsDao().getSummaryPrice());
        } else if ("count".equals(command)) {
            return HtmlBuildingUtils.buildStats("Number of products: ", getProductsDao().getProductsCount());
        } else {
            return "Unknown command: " + command;
        }
    }

}
