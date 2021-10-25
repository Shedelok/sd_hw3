package ru.akirakozov.sd.refactoring;

import ru.akirakozov.sd.refactoring.entity.Product;

import java.util.List;
import java.util.Optional;

public class HtmlBuildingUtils {
    private HtmlBuildingUtils() {
    }

    public static String buildHtml(String bodyContent) {
        return "<html><body>" + System.lineSeparator() + bodyContent + "</body></html>";
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static String buildListOfProducts(Optional<String> headerContent, List<Product> products) {
        StringBuilder bodyContentBuilder = new StringBuilder();
        headerContent.ifPresent(h ->
                bodyContentBuilder.append("<h1>").append(h).append("</h1>").append(System.lineSeparator())
        );
        for (Product product : products) {
            bodyContentBuilder.append(product.getName()).append("\t").append(product.getPrice()).append("</br>")
                    .append(System.lineSeparator());
        }
        return buildHtml(bodyContentBuilder.toString());
    }
}
