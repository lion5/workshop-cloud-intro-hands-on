/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.getstarted.basicactions;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.daos.CloudSqlDao;
import com.example.getstarted.objects.Book;
import com.example.getstarted.objects.Result;
import com.example.getstarted.util.BucketHelper;

import com.example.getstarted.util.S3Helper;
import com.google.common.base.Strings;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
// a url pattern of "" makes this servlet the root servlet
@WebServlet(name = "list", urlPatterns = {"", "/books"}, loadOnStartup = 1)
@SuppressWarnings("serial")
public class ListBookServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ListBookServlet.class.getName());
    private static final String BUCKET_NAME = System.getenv("BOOKSHELF_BUCKET");
    private static final String BOOKSHELF_STORAGE_TYPE = System.getenv("BOOKSHELF_STORAGE_TYPE");
    private static final String SQL_HOST = System.getenv("SQL_HOST");
    private static final String SQL_PORT = System.getenv("SQL_PORT");
    private static final String SQL_DB_NAME = System.getenv("SQL_DB_NAME");
    private static final String SQL_USER_NAME = System.getenv("SQL_USER_NAME");
    private static final String SQL_PASSWORD = System.getenv("SQL_PASSWORD");
    private static final String SQL_REGION = System.getenv("SQL_REGION");

    private static final String SQL_INSTANCE_NAME = SQL_REGION + ":" + SQL_HOST;

    private BookDao dao = null;

    @Override
    public void init() throws ServletException {
        String SQL_URL = "";
        BucketHelper storageHelper;

        // Bucket helper to store and get Urls of S3 objects
        if (!Strings.isNullOrEmpty(BUCKET_NAME)){
            storageHelper = new S3Helper();
            this.getServletContext().setAttribute("storageHelper", storageHelper);
            this.getServletContext().setAttribute(
                    "isCloudStorageConfigured",    // Hide upload when Cloud Storage is not configured.
                    !Strings.isNullOrEmpty(BUCKET_NAME));
        }

        // MySQL Database connection information
        SQL_URL = "jdbc:mysql://" + SQL_HOST + ":" +
                SQL_PORT + "/" + SQL_DB_NAME + "?user=" + SQL_USER_NAME + "&password=" + SQL_PASSWORD;

        // Creates the DAO based on the Context Parameters and stores in the Servlet Context
        logger.log(Level.INFO, "SQL URL REMOTE: " + SQL_URL + " DB_INSTANCE_NAME=" + SQL_INSTANCE_NAME);
        if (dao == null) {
            switch (BOOKSHELF_STORAGE_TYPE) {
                case "cloudsql":
                    try {
                        dao = new CloudSqlDao(SQL_URL);
                    } catch (SQLException e) {
                        throw new ServletException("SQL error", e);
                    }
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid storage type. Check if bookshelf.storageType property is set.");
            }
        }
        this.getServletContext().setAttribute("dao", dao);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            ServletException {
        BucketHelper storageHelper =
                (BucketHelper) req.getServletContext().getAttribute("storageHelper");

        BookDao dao = (BookDao) this.getServletContext().getAttribute("dao");
        String startCursor = req.getParameter("cursor");
        List<Book> books = null;
        String endCursor = null;
        try {
            Result<Book> result = dao.listBooks(startCursor);
            logger.log(Level.INFO, "Retrieved list of all books");
            books = result.result;
            // set image resized urls
            for (Book book : books) {
                String mediaUrl = storageHelper.searchMediaUrl(book.getObjectKey(), BUCKET_NAME);
                if(mediaUrl==null || "".equals(mediaUrl)){
                    mediaUrl = storageHelper.searchMediaUrl("rawimg-" + book.getObjectKey(), BUCKET_NAME);
                }
                logger.info("Media URL for display: " + mediaUrl);
                book.setMediaUrl(mediaUrl);
            }
            endCursor = result.cursor;
        } catch (Exception e) {
            throw new ServletException("Error listing books", e);
        }
        req.getSession().getServletContext().setAttribute("books", books);
        StringBuilder bookNames = new StringBuilder();
        for (Book book : books) {
            bookNames.append(book.getTitle() + ", ");
        }
        logger.log(Level.INFO, "Loaded books: " + bookNames.toString());
        req.setAttribute("cursor", endCursor);
        req.setAttribute("page", "list");
        req.getRequestDispatcher("/base.jsp").forward(req, resp);
    }

    public void setDao(CloudSqlDao dao) {
        this.dao = dao;
    }
}
// [END example]