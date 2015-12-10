package com.atlassian.jira.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;

/**
 * This class only resposibility is to fetch implemmentaion from DI
 * contaier and forward request to it.
 */
public class ViewUniversalAvatarServlet extends HttpServlet
{
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final ViewUniversalAvatarServletImpl viewAvatarServlet = ComponentAccessor.getComponent(ViewUniversalAvatarServletImpl.class);
        viewAvatarServlet.doGet(request, response);
    }
}
