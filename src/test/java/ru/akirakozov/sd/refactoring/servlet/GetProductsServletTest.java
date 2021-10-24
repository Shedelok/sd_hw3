package ru.akirakozov.sd.refactoring.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akirakozov.sd.refactoring.ProductsDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class GetProductsServletTest {
    @Mock
    private ProductsDao productsDao;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private GetProductsServlet getProductsServlet;

    @BeforeEach
    void setUp() {
        getProductsServlet = new GetProductsServlet(productsDao);
    }

    @Test
    void sqlException() throws Exception {
        doThrow(SQLException.class).when(productsDao).getAllProducts(response);

        assertThrows(RuntimeException.class, () -> getProductsServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }
}