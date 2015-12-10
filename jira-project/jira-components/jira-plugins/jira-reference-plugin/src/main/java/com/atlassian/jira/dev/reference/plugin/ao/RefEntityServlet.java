package com.atlassian.jira.dev.reference.plugin.ao;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import com.atlassian.jira.util.dbc.Assertions;

public class RefEntityServlet extends HttpServlet
{
    private final RefEntityService refEntityService;

    public RefEntityServlet(RefEntityService refEntityService)
    {
        this.refEntityService = Assertions.notNull(refEntityService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final PrintWriter w = res.getWriter();
        w.write("<h1>Reference Entities</h1>");

        // the form to post more RefEntitys
        w.write("<form method=\"post\">");
        w.write("<input type=\"text\" name=\"description\" size=\"25\"/>");
        w.write("&nbsp;&nbsp;");
        w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
        w.write("</form>");

        w.write("<ol>");

        for (RefEntity refEntity : refEntityService.allEntities()) // (2)
        {
            w.printf("<li>%s</li>", refEntity.getDescription());
        }

        w.write("</ol>");
        w.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final String description = req.getParameter("description");
        refEntityService.add(description);
        res.sendRedirect(req.getContextPath() + "/plugins/servlet/refentity/list");
    }
}
