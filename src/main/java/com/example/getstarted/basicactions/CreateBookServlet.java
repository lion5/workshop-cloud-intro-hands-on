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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.getstarted.daos.BookDao;
import com.example.getstarted.objects.Book;
import com.example.getstarted.util.BucketHelper;

// [START example]
@SuppressWarnings("serial")
// [START annotations]
@MultipartConfig
@WebServlet(name = "create", urlPatterns = {"/create"})
// [END annotations]
public class CreateBookServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(CreateBookServlet.class.getName());
    private final String BUCKET_NAME = System.getenv("BOOKSHELF_BUCKET");

    // [START setup]
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        req.setAttribute("action", "Add");          // Part of the Header in form.jsp
        req.setAttribute("destination", "create");  // The urlPattern to invoke (this Servlet)
        req.setAttribute("page", "form");           // Tells base.jsp to include form.jsp
        req.getRequestDispatcher("/base.jsp").forward(req, resp);
    }
    // [END setup]

    // [START formpost]
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        BucketHelper storageHelper =
                (BucketHelper) req.getServletContext().getAttribute("storageHelper");
        String objectKey =
                storageHelper.getObjectKey(
                        req, resp, BUCKET_NAME);

        String mediaUrl = storageHelper.searchMediaUrl(objectKey, BUCKET_NAME);
        logger.info("Got Media Url: " + mediaUrl);

        String createdByString = "";
        String createdByIdString = "";
        if (req.getSession().getAttribute("token") != null) { // Does the user have a logged in session?
            createdByString = (String) req.getSession().getAttribute("userEmail");
            createdByIdString = (String) req.getSession().getAttribute("userId");
        }

        BookDao dao = (BookDao) this.getServletContext().getAttribute("dao");

        Book book = new Book.Builder()
                .author(req.getParameter("author"))   // form parameter

                .createdBy(createdByString)
                .createdById(createdByIdString)

                .description(req.getParameter("description"))
                .publishedDate(req.getParameter("publishedDate"))
                .title(req.getParameter("title"))
                .objectKey(objectKey)
                .mediaUrl(mediaUrl)
                .build();
        try {
            Long id = dao.createBook(book);
            logger.log(Level.INFO, "Created book {0}", book);
            resp.sendRedirect("/read?id=" + id.toString());   // read what we just wrote
        } catch (Exception e) {
            throw new ServletException("Error creating book", e);
        }
    }
    // [END formpost]
}

// [END example]

