package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.util.dbc.Assertions;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.ws.rs.core.UriBuilder;

/**
 * Builder for AttachmentBean instances.
 *
 * @since v4.2
 */
public class AttachmentBeanBuilder
{
    /**
     * The attachment for which we want to create an AttachmentBean.
     */
    private final Attachment attachment;

    /**
     * Instance of a simple component for getting the base url of the app.
     */
    private final JiraBaseUrls jiraBaseUrls;

    /**
     * The ThumbnailManager.
     */
    private final ThumbnailManager thumbnailManager;

    /**
     * Creates a new AttachmentBeanBuilder.
     *
     * @param jiraBaseUrls a JiraBaseUrls
     * @param thumbnailManager a ThumbnailManager
     * @param attachment an Attachment
     */
    public AttachmentBeanBuilder(JiraBaseUrls jiraBaseUrls, ThumbnailManager thumbnailManager, Attachment attachment)
    {
        this.jiraBaseUrls = Assertions.notNull("jiraBaseUrls", jiraBaseUrls);
        this.thumbnailManager = Assertions.notNull("thumbnailManager", thumbnailManager);
        this.attachment = Assertions.notNull("attachment", attachment);
    }

    /**
     * Builds a new AttachmentBean.
     *
     * @return a new AttachmentBean instance
     * @throws IllegalStateException if any of this builder's properties is not set
     */
    public AttachmentBean build() throws IllegalStateException
    {
        if (attachment == null) { throw new IllegalStateException("attachment not set"); }

        try
        {
            URI self = UriBuilder.fromPath(jiraBaseUrls.restApi2BaseUrl()).path(AttachmentResource.class).path(attachment.getId().toString()).build();
            String filename = attachment.getFilename();
            UserBean author = new UserBeanBuilder(jiraBaseUrls).user(attachment.getAuthorObject()).buildShort();
            Timestamp created = attachment.getCreated();
            Long size = attachment.getFilesize();
            String mimeType = attachment.getMimetype();
            HashMap<String, Object> properties = new PropertySetAdapter().marshal(attachment.getProperties());

            String encodedFilename = URLEncoder.encode(attachment.getFilename(), "UTF-8");
            String content = String.format("%s/secure/attachment/%s/%s", jiraBaseUrls.baseUrl(), attachment.getId(), encodedFilename);

            ThumbnailedImage thumbnail = thumbnailManager.toThumbnailedImage(thumbnailManager.getThumbnail(attachment));
            String thumbnailURL = thumbnail != null ? thumbnail.getImageURL() : null;
            return new AttachmentBean(self, filename, author, created, size, mimeType, properties, content, thumbnailURL);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error encoding file name", e);
        }
    }
}
