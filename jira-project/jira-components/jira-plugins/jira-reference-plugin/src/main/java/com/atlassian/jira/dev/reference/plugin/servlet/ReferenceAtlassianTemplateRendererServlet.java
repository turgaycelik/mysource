package com.atlassian.jira.dev.reference.plugin.servlet;

import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a reference servlet, it is used to test the rendering of templates through the atlassian template renderer.
 *
 * @since v4.4
 */
public class ReferenceAtlassianTemplateRendererServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;

    public ReferenceAtlassianTemplateRendererServlet(final TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        templateRenderer.render("templates/atlassian-template-renderer-view.vm", resp.getWriter());
    }
}
