/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.io.InputStreamConsumer;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.atlassian.blobstore.client.api.Unit;

public class ViewThumbnailServlet extends ViewAttachmentServlet
{
    @Override
    protected void getInputStream(final String attachmentPath, final InputStreamConsumer<Unit> consumer)
            throws InvalidAttachmentPathException, DataAccessException, IOException, PermissionException
    {
        Attachment attachment = getAttachment(attachmentPath);

        if (!hasPermissionToViewAttachment(getUserName(), attachment))
        {
            throw new PermissionException("You do not have permissions to view this attachment");
        }

        ComponentAccessor.getComponentOfType(ThumbnailManager.class).streamThumbnailContent(attachment, consumer);
    }

    protected void setResponseHeaders(HttpServletRequest request, HttpServletResponse response)
    {
        Attachment attachment = getAttachment(attachmentQuery(request));
        File thumbnailFile = AttachmentUtils.getThumbnailFile(attachment);
        // All thumbnail images are stored in JPEG format.
        response.setContentType(ThumbnailManager.MIME_TYPE.toString());
        response.setContentLength((int) thumbnailFile.length());
        response.setHeader("Content-Disposition", "inline; filename=" + thumbnailFile.getName() + ";");

        HttpResponseHeaders.cachePrivatelyForAboutOneYear(response);
    }
}
