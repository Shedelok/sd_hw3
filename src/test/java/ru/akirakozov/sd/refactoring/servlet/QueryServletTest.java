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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryServletTest {
    @Mock
    private ProductsDao productsDao;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private QueryServlet queryServlet;

    @BeforeEach
    void setUp() {
        queryServlet = new QueryServlet(productsDao);
    }

    @Test
    void max_sqlException() throws Exception {
        when(request.getParameter("command")).thenReturn("max");
        doThrow(SQLException.class).when(productsDao).getProductWithMaxPrice();

        assertThrows(RuntimeException.class, () -> queryServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }

    @Test
    void min_sqlException() throws Exception {
        when(request.getParameter("command")).thenReturn("min");
        doThrow(SQLException.class).when(productsDao).getProductWithMinPrice();

        assertThrows(RuntimeException.class, () -> queryServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }

    @Test
    void sum_sqlException() throws Exception {
        when(request.getParameter("command")).thenReturn("sum");
        doThrow(SQLException.class).when(productsDao).getSummaryPrice();

        assertThrows(RuntimeException.class, () -> queryServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }

    @Test
    void count_sqlException() throws Exception {
        when(request.getParameter("command")).thenReturn("count");
        doThrow(SQLException.class).when(productsDao).getProductsCount();

        assertThrows(RuntimeException.class, () -> queryServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }
}