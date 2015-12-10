/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.web.exception.WebExceptionChecker;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.atlassian.blobstore.client.api.Unit;

public abstract class AbstractViewFileServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ViewAttachmentServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            String attachmentQuery;
            try
            {
                attachmentQuery = attachmentQuery(request);
            }
            catch (InvalidAttachmentPathException e)
            {
                response.sendError(400, "Invalid attachment path");
                return;
            }
            catch (AttachmentNotFoundException nfe)
            {
                send404(request, response);
                return;
            }

            streamFileData(request, response, attachmentQuery);
        }
        catch (Exception e)
        {
            if (WebExceptionChecker.canBeSafelyIgnored(e))
            {
                return;
            }
            log.error("Error serving file for path " + request.getPathInfo() + ": " + e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    private void send404(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendError(404, String.format("Attachment %s was not found", request.getPathInfo()));
    }

    private void redirectForSecurityBreach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (getUserName() != null)
        {
            RequestDispatcher rd = request.getRequestDispatcher("/secure/views/securitybreach.jsp");
            JiraHttpUtils.setNoCacheHeaders(response);
            rd.forward(request, response);
        }
        else
        {
            response.sendRedirect(RedirectUtils.getLoginUrl(request));
        }
    }

    private void streamFileData(final HttpServletRequest request, final HttpServletResponse response, final String attachmentPath)
            throws IOException, ServletException
    {
        try
        {
            getInputStream(attachmentPath, new InputStreamConsumer<Unit>() {
                @Override
                public Unit withInputStream(final InputStream is) throws IOException
                {
                    // can only set headers after knowing that we have the file - otherwise we can't do response.sendError()
                    setResponseHeaders(request, response);

                    final OutputStream out = response.getOutputStream();
                    try
                    {
                        IOUtils.copy(is, out);
                    }
                    finally
                    {
                        IOUtils.closeQuietly(out);
                    }
                    return Unit.UNIT;
                }
            });
        }
        catch (AttachmentNotFoundException e)
        {
            log.error("Error finding " + request.getPathInfo() + " : " + e.getMessage());
            // the outcome of this will only be complete if nothing else has written to the OutputStream, so we must
            // do this before setResponseHeaders() is called
            send404(request, response);
            return;
        }
        catch (FileNotFoundException e) // getInputStream will throw it when file is missing on disk
        {
            log.error("Error finding " + request.getPathInfo() + " : " + e.getMessage());
            // the outcome of this will only be complete if nothing else has written to the OutputStream, so we must
            // do this before setResponseHeaders() is called
            send404(request, response);
            return;
        }
        catch (IOException e)
        {
            // we suspect this to be a Broken Pipe exception, probably due to the user closing the connection by pressing
            // the stop button in their browser, which we don't really care about logging
            if (log.isDebugEnabled())
            {
                log.debug("Error serving content to client", e);
            }
        }
        catch (PermissionException e)
        {
            redirectForSecurityBreach(request, response);
            return;
        }
    }

    /**
     * Validates that path is valid attachment path.
     *
     * @param request HTTP request
     * @return attachment path
     */
    protected final String attachmentQuery(final HttpServletRequest request)
    {
        String pi = request.getPathInfo();
        if (pi == null || pi.length() == 1 || pi.indexOf('/', 1) == -1)
        {
            throw new InvalidAttachmentPathException();
        }
        return pi;
    }

    /**
     * Gets the attachment file (not the file name) that corresponds to the requested attachment.
     *
     * @param attachmentPath the attachment path
     * @return the File resource for the attachment.
     * @throws DataAccessException If there is a problem looking up the data to support the attachment.
     * @throws IOException if there is a problem getting the File.
     * @throws PermissionException if the user has insufficient permission to see the attachment.
     * @throws InvalidAttachmentPathException if the path to the attachment was invalid in some way.
     */
    protected abstract void getInputStream(String attachmentPath, InputStreamConsumer<Unit> consumer)
            throws InvalidAttachmentPathException, DataAccessException, IOException, PermissionException;

    /**
     * Sets the content type, content length and "Content-Disposition" header
     * of the response based on the values of the attachement found.
     *
     * @param request  HTTP request
     * @param response HTTP response
     */
    protected abstract void setResponseHeaders(HttpServletRequest request, HttpServletResponse response)
            throws InvalidAttachmentPathException, DataAccessException, IOException;

    /**
     * @return The logged-in user's name, or null (anonymous)
     */
    protected final String getUserName()
    {
        ApplicationUser user = getJiraAuthenticationContext().getUser();
        return (user != null ? user.getUsername() : null);
    }

    protected JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }
}
