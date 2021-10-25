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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddProductServletTest {
    @Mock
    private ProductsDao productsDao;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AddProductServlet addProductServlet;

    @BeforeEach
    void setUp() {
        addProductServlet = new AddProductServlet(productsDao);
    }

    @Test
    void sqlException() throws Exception {
        when(request.getParameter(any())).thenAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            if (argument.equals("name")) {
                return "product";
            } else if (argument.equals("price")) {
                return "123";
            }

            fail();
            throw new RuntimeException();
        });
        doThrow(SQLException.class).when(productsDao).insertProduct(any(), anyLong());

        assertThrows(RuntimeException.class, () -> addProductServlet.doGet(request, response));

        verifyNoMoreInteractions(response);
    }
}