package ru.akirakozov.sd.refactoring;

import org.junit.jupiter.api.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final int PORT = 8081;
    private static final int SERVER_CONNECTION_RETRIES = 20;
    private static final int SERVER_CONNECTION_TIMEOUT_MILLIS = 200;
    private static final int SERVER_TERMINATION_JOIN_TIMEOUT_MILLIS = 3000;

    private static Thread serverThread;

    private static void withMethodRequest(String urlSuffix, HttpURLConnectionConsumer consumer) throws Exception {
        Optional<HttpURLConnection> connection = Optional.empty();
        try {
            connection = Optional.of(
                    (HttpURLConnection) new URL("http://localhost:" + PORT + "/" + urlSuffix).openConnection()
            );
            consumer.accept(connection.get());
        } finally {
            connection.ifPresent(HttpURLConnection::disconnect);
        }
    }

    private static void withGetProductsRequest(HttpURLConnectionConsumer consumer) throws Exception {
        withMethodRequest("get-products", connection -> {
            assertEquals(HttpServletResponse.SC_OK, connection.getResponseCode());
            assertTrue(connection.getContentType().startsWith("text/html;"));
            consumer.accept(connection);
        });
    }

    private static void sendAddProductRequest(AddProductParameters... parameters) throws Exception {
        withAddProductRequest(parameters, connection -> {
            assertEquals(HttpServletResponse.SC_OK, connection.getResponseCode());
            assertTrue(connection.getContentType().startsWith("text/html;"));
            assertEquals("OK\n", readInputStream(connection.getInputStream()));
        });
    }

    private static void withAddProductRequest(
            AddProductParameters[] parameters,
            HttpURLConnectionConsumer consumer
    ) throws Exception {
        StringBuilder urlBuilder = new StringBuilder("add-product");
        for (int i = 0; i < parameters.length; i++) {
            AddProductParameters p = parameters[i];
            urlBuilder.append(i == 0 ? '?' : '&');
            urlBuilder.append("name").append('=').append(p.name).append('&')
                    .append("price").append('=').append(p.price);
        }

        withMethodRequest(urlBuilder.toString(), consumer);
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            resultStream.write(buffer, 0, length);
        }
        return resultStream.toString();
    }

    @BeforeAll
    static void startServer() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS PRODUCT");
            }
        }

        serverThread = new Thread(() -> {
            try {
                Main.main(new String[0]);
            } catch (InterruptedException e) {
                // ok, finishing tests
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // making sure that server is up
        for (int i = 0; i < SERVER_CONNECTION_RETRIES; i++) {
            try {
                new URL("http://localhost:" + PORT).openConnection().connect();
                return;
            } catch (ConnectException e) {
                Thread.sleep(SERVER_CONNECTION_TIMEOUT_MILLIS);
            }
        }
        fail();
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate("DELETE FROM PRODUCT");
            }
        }
    }

    @AfterAll
    static void interruptServer() {
        serverThread.interrupt();
        try {
            serverThread.join(SERVER_TERMINATION_JOIN_TIMEOUT_MILLIS);
        } catch (Exception e) {
            fail("Failed to join the server thread", e);
        }
    }

    @Test
    void getProductsOnEmptyTable() throws Exception {
        withGetProductsRequest(connection -> assertEquals(
                "<html><body>\n</body></html>\n",
                readInputStream(connection.getInputStream())
        ));
    }

    @Nested
    class AddProductTest {
        @Test
        void single() throws Exception {
            sendAddProductRequest(new AddProductParameters("product", "123"));

            withGetProductsRequest(connection -> assertEquals(
                    "<html><body>\nproduct\t123</br>\n</body></html>\n",
                    readInputStream(connection.getInputStream())
            ));
        }

        @Test
        void two() throws Exception {
            sendAddProductRequest(new AddProductParameters("product1", "1"));
            sendAddProductRequest(new AddProductParameters("product2", "10"));

            withGetProductsRequest(connection -> assertEquals(
                    "<html><body>\nproduct1\t1</br>\nproduct2\t10</br>\n</body></html>\n",
                    readInputStream(connection.getInputStream())
            ));
        }


        @Test
        void twoIdentical() throws Exception {
            sendAddProductRequest(new AddProductParameters("product", "123"));
            sendAddProductRequest(new AddProductParameters("product", "123"));

            withGetProductsRequest(connection -> assertEquals(
                    "<html><body>\nproduct\t123</br>\nproduct\t123</br>\n</body></html>\n",
                    readInputStream(connection.getInputStream())
            ));
        }

        @Test
        void withoutParameters() throws Exception {
            withAddProductRequest(new AddProductParameters[0], connection ->
                    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, connection.getResponseCode())
            );
        }

        @Test
        void invalidPrice() throws Exception {
            withAddProductRequest(new AddProductParameters[]{new AddProductParameters("product", "abc")}, connection ->
                    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, connection.getResponseCode())
            );
        }
    }

    @Nested
    class QueryTest {
        private void withQueryRequest(String command, HttpURLConnectionConsumer consumer) throws Exception {
            withMethodRequest("query?command=" + command, connection -> {
                assertEquals(HttpServletResponse.SC_OK, connection.getResponseCode());
                assertTrue(connection.getContentType().startsWith("text/html;"));
                consumer.accept(connection);
            });
        }

        @Nested
        class MaxTest {
            private void withQueryMaxRequest(HttpURLConnectionConsumer consumer) throws Exception {
                withQueryRequest("max", consumer);
            }

            @Test
            void emptyTable() throws Exception {
                withQueryMaxRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with max price: </h1>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void singleProduct() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQueryMaxRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with max price: </h1>\nproduct\t123</br>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void multipleProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "1"));
                sendAddProductRequest(new AddProductParameters("product2", "3"));
                sendAddProductRequest(new AddProductParameters("product3", "2"));

                withQueryMaxRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with max price: </h1>\nproduct2\t3</br>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void twoProductsWithMaxPrice() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "123"));
                sendAddProductRequest(new AddProductParameters("product2", "123"));

                withQueryMaxRequest(connection -> {
                    String body = readInputStream(connection.getInputStream());
                    assertTrue(body.equals(
                                    "<html><body>\n<h1>Product with max price: </h1>\nproduct1\t123</br>\n</body></html>\n"
                            ) || body.equals(
                                    "<html><body>\n<h1>Product with max price: </h1>\nproduct2\t123</br>\n</body></html>\n"
                            )
                    );
                });
            }
        }

        @Nested
        class MinTest {
            private void withQueryMinRequest(HttpURLConnectionConsumer consumer) throws Exception {
                withQueryRequest("min", consumer);
            }

            @Test
            void emptyTable() throws Exception {
                withQueryMinRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with min price: </h1>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void singleProduct() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQueryMinRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with min price: </h1>\nproduct\t123</br>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void multipleProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "1"));
                sendAddProductRequest(new AddProductParameters("product2", "3"));
                sendAddProductRequest(new AddProductParameters("product3", "2"));

                withQueryMinRequest(connection -> assertEquals(
                        "<html><body>\n<h1>Product with min price: </h1>\nproduct1\t1</br>\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void twoProductsWithMinPrice() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "123"));
                sendAddProductRequest(new AddProductParameters("product2", "123"));

                withQueryMinRequest(connection -> {
                    String body = readInputStream(connection.getInputStream());
                    assertTrue(body.equals(
                                    "<html><body>\n<h1>Product with min price: </h1>\nproduct1\t123</br>\n</body></html>\n"
                            ) || body.equals(
                                    "<html><body>\n<h1>Product with min price: </h1>\nproduct2\t123</br>\n</body></html>\n"
                            )
                    );
                });
            }
        }

        @Nested
        class SumTest {
            private void withQuerySumRequest(HttpURLConnectionConsumer consumer) throws Exception {
                withQueryRequest("sum", consumer);
            }

            @Test
            void emptyTable() throws Exception {
                withQuerySumRequest(connection -> assertEquals(
                        "<html><body>\nSummary price: \n0\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void singleProduct() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQuerySumRequest(connection -> assertEquals(
                        "<html><body>\nSummary price: \n123\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void multipleProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "2"));
                sendAddProductRequest(new AddProductParameters("product2", "10"));
                sendAddProductRequest(new AddProductParameters("product3", "2"));

                withQuerySumRequest(connection -> assertEquals(
                        "<html><body>\nSummary price: \n14\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void identicalProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQuerySumRequest(connection -> assertEquals(
                        "<html><body>\nSummary price: \n246\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }
        }

        @Nested
        class CountTest {
            private void withQueryCountRequest(HttpURLConnectionConsumer consumer) throws Exception {
                withQueryRequest("count", consumer);
            }

            @Test
            void emptyTable() throws Exception {
                withQueryCountRequest(connection -> assertEquals(
                        "<html><body>\nNumber of products: \n0\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void singleProduct() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQueryCountRequest(connection -> assertEquals(
                        "<html><body>\nNumber of products: \n1\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void multipleProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product1", "2"));
                sendAddProductRequest(new AddProductParameters("product2", "10"));
                sendAddProductRequest(new AddProductParameters("product3", "2"));

                withQueryCountRequest(connection -> assertEquals(
                        "<html><body>\nNumber of products: \n3\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }

            @Test
            void identicalProducts() throws Exception {
                sendAddProductRequest(new AddProductParameters("product", "123"));
                sendAddProductRequest(new AddProductParameters("product", "123"));

                withQueryCountRequest(connection -> assertEquals(
                        "<html><body>\nNumber of products: \n2\n</body></html>\n",
                        readInputStream(connection.getInputStream())
                ));
            }
        }

        @Test
        void unknownCommand() throws Exception {
            withQueryRequest("unknownCommand", connection ->
                    assertEquals("Unknown command: unknownCommand\n", readInputStream(connection.getInputStream()))
            );
        }
    }

    private interface HttpURLConnectionConsumer {
        void accept(HttpURLConnection connection) throws Exception;
    }

    private static class AddProductParameters {
        private final String name;
        private final String price;

        private AddProductParameters(String name, String price) {
            this.name = name;
            this.price = price;
        }
    }
}