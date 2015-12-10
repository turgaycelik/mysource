package com.atlassian.jira.dev.reference.plugin.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a simple servlet that prints out a simple message and the user request parameter's value.
 *
 * @since v4.3
 */
public class ReferenceAttributeServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.getWriter().write(
                String.format("%sHello, from ReferenceServletContextListener %s%s",
                        "<html><body>",
                        getServletContext().getAttribute("reference"),
                        "</body></html>"
                )
        );

    }
}
