package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

/**
 * @author akirakozov
 */
public class Main {
    private static final String PRODUCTION_DATABASE_URL = "jdbc:sqlite:prod.db";

    public static void main(String[] args) throws Exception {
        String databaseUrl = args.length >= 1 && args[0] != null ? args[0] : PRODUCTION_DATABASE_URL;
        ProductsDao productsDao = new ProductsDao(databaseUrl);
        productsDao.createDatabaseIfNotExist();

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(productsDao)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(productsDao)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(productsDao)),"/query");

        server.start();
        server.join();
    }
}
