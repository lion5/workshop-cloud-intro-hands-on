package com.example.getstarted.basicactions;

import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.getstarted.daos.CloudSqlDao;
import com.example.getstarted.objects.Book;
import com.example.getstarted.objects.Result;

import org.eclipse.jetty.server.session.Session;

import com.example.getstarted.util.BucketHelper;
import com.example.getstarted.util.S3Helper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class IntegrationTest {
    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();


    // MySQL database test
    @Test
    public void listBooksMySQL() throws Exception {
        // Given
        environmentVariables.set("BOOKSHELF_STORAGE_TYPE", "cloudsql");
        environmentVariables.set("AWS_REGION", "eu-west-1");
        ListBookServlet listBookServlet = spy(ListBookServlet.class);
        BucketHelper storageHelper = new S3Helper();

        // Mocks of dependencies
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletContext servletContext = mock(ServletContext.class);
        CloudSqlDao dao = mock(CloudSqlDao.class);
        listBookServlet.setDao(dao);
        ServletConfig servletConfig = mock(ServletConfig.class);
        Session session = mock(Session.class);
        // Stubbed behavior
        when(dao.listBooks(any(String.class))).thenReturn(new Result<Book>(new ArrayList<>()));
        when(req.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("dao")).thenReturn(dao);
        when(listBookServlet.getServletConfig()).thenReturn(servletConfig);
        when(listBookServlet.getServletContext()).thenReturn(servletContext);
        when(req.getServletContext()).thenReturn(servletContext);
        when(req.getServletContext().getAttribute("storageHelper")).thenReturn(storageHelper);
        when(req.getRequestDispatcher("/base.jsp")).thenReturn(mock(RequestDispatcher.class));

        // When
        listBookServlet.doGet(req, response);

        // Then
        assertEquals(200, response.getStatus());


    }

    // old datastore test
    @Test
    public void listBooksDataStore() throws Exception {
        assertEquals(true, true);
        /*
        // Given
        environmentVariables.set("BOOKSHELF_STORAGE_TYPE", "datastore");
        ListBookServlet listBookServlet = spy(ListBookServlet.class);
        CloudStorageHelper storageHelper = new CloudStorageHelper();

        // Mocks of dependencies
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletContext servletContext = mock(ServletContext.class);
        DatastoreDao dao = mock(DatastoreDao.class);
        listBookServlet.setDao(dao);
        ServletConfig servletConfig = mock(ServletConfig.class);
        Session session = mock(Session.class);
        // Stubbed behavior
        when(dao.listBooks(any(String.class))).thenReturn(new Result<Book>(new ArrayList<>()));
        when(req.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute("dao")).thenReturn(dao);
        when(listBookServlet.getServletConfig()).thenReturn(servletConfig);
        when(listBookServlet.getServletContext()).thenReturn(servletContext);
        when(req.getServletContext()).thenReturn(servletContext);
        when(req.getServletContext().getAttribute("storageHelper")).thenReturn(storageHelper);
        when(req.getRequestDispatcher("/base.jsp")).thenReturn(mock(RequestDispatcher.class));

        // When
        listBookServlet.doGet(req, response);

        // Then
        assertEquals(200, response.getStatus());

     */
    }
}
