/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.io.InputStreamConsumer;

import io.atlassian.blobstore.client.api.Unit;

import static com.atlassian.jira.security.Permissions.BROWSE;
import static com.atlassian.jira.util.BrowserUtils.USER_AGENT_HEADER;

public class ViewAttachmentServlet extends AbstractViewFileServlet
{
    /**
     * Returns the file of the attachment.
     *
     * @param attachmentQuery the query describing which attachment to get
     * @throws DataAccessException if attachment or user cannot be retrieved due to some kind of db access problem.
     * @throws PermissionException if user is denied permission to see the attachment
     */
    protected void getInputStream(String attachmentQuery, InputStreamConsumer<Unit> consumer)
            throws DataAccessException, PermissionException, IOException
    {
        Attachment attachment = getAttachment(attachmentQuery);

        if (!hasPermissionToViewAttachment(getUserName(), attachment))
        {
            throw new PermissionException("You do not have permissions to view this issue");
        }

        ComponentAccessor.getAttachmentManager().streamAttachmentContent(attachment, consumer);
    }

    /**
     * Looks up the attachment by reading the id from the query string.
     *
     * @param query eg. '/10000/foo.txt'
     * @return attachment found
     */
    protected Attachment getAttachment(String query)
    {
        int x = query.indexOf('/', 1);
        final String idStr = query.substring(1, x);
        Long id;
        try
        {
            id = new Long(idStr);
        }
        catch (NumberFormatException e)
        {
            throw new AttachmentNotFoundException(idStr);
        }
        if (query.indexOf('/', x+1) != -1)
        {
            // JRA-14580. only one slash is allowed to prevent infinite recursion by web crawlers.
            throw new AttachmentNotFoundException(idStr);
        }

        return ComponentAccessor.getAttachmentManager().getAttachment(id);
    }

    /**
     * Sets the content type, content length and "Content-Disposition" header
     * of the response based on the values of the attachement found.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @throws AttachmentNotFoundException
     * @throws IOException
     */
    protected void setResponseHeaders(HttpServletRequest request, HttpServletResponse response)
            throws AttachmentNotFoundException, IOException
    {
        final Attachment attachment = getAttachment(attachmentQuery(request));
        response.setContentType(attachment.getMimetype());
        response.setContentLength(attachment.getFilesize().intValue());

        getMimeSniffingKit().setAttachmentResponseHeaders(attachment, request.getHeader(USER_AGENT_HEADER), response);
        HttpResponseHeaders.cachePrivatelyForAboutOneYear(response);
    }

    /**
     * Gets MimeSniffingKit from PICO container, you should not cache it in the servlet because servlets have a different
     * lifecycle than PICO.
     */
    private MimeSniffingKit getMimeSniffingKit()
    {
        return ComponentAccessor.getComponent(MimeSniffingKit.class);
    }

    /**
     * Checks if the given user had permission to see the attachemnt.
     *
     * @param username   username of the user who wants to see the attachment
     * @param attachment attachment to be checked
     * @return true if user can see the attachment, false otherwise
     * @throws DataAccessException if no such user exists.
     */
    protected boolean hasPermissionToViewAttachment(String username, Attachment attachment) throws DataAccessException
    {
        Issue issue = attachment.getIssueObject();
        if (username == null)
        {
            return (ComponentAccessor.getPermissionManager().hasPermission(BROWSE, issue, (ApplicationUser) null));
        }
        final ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(username);
        if (user == null)
        {
            throw new DataAccessException("User '"+ username + "' not found");
        }
        return (ComponentAccessor.getPermissionManager().hasPermission(BROWSE, issue, user));
    }
}
