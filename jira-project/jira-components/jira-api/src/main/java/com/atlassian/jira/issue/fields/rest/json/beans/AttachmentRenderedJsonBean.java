package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Same as {@link AttachmentJsonBean} but contains rendered data
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class AttachmentRenderedJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String filename;

    @JsonProperty
    private UserJsonBean author;

    @JsonProperty
    private String created;

    @JsonProperty
    private String size;

    @JsonProperty
    private String mimeType;

    @JsonProperty
    private String content;

    @JsonProperty
    private String thumbnail;

    public String getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getFilename()
    {
        return filename;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public String getCreated()
    {
        return created;
    }

    public String getSize()
    {
        return size;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public String getContent()
    {
        return content;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    /**
     * @deprecated Use {@link #shortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.issue.thumbnail.ThumbnailManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<AttachmentRenderedJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager, final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        return shortBeans(attachments, urls, thumbnailManager, dateTimeFormatterFactory, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<AttachmentRenderedJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager, final DateTimeFormatterFactory dateTimeFormatterFactory, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Collection<AttachmentRenderedJsonBean> result = Lists.newArrayListWithCapacity(attachments.size());
        for (Attachment from : attachments)
        {
            result.add(shortBean(from, urls, thumbnailManager, dateTimeFormatterFactory, loggedInUser, emailFormatter));
        }

        return result;
    }

    /**
     * @deprecated Use {@link #shortBean(com.atlassian.jira.issue.attachment.Attachment, JiraBaseUrls, com.atlassian.jira.issue.thumbnail.ThumbnailManager, com.atlassian.jira.datetime.DateTimeFormatterFactory, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static AttachmentRenderedJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        return shortBean(attachment, urls, thumbnailManager, dateTimeFormatterFactory, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     * @return null if the input is null
     */
    public static AttachmentRenderedJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager, DateTimeFormatterFactory dateTimeFormatterFactory, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (attachment == null)
        {
            return null;
        }
        final AttachmentRenderedJsonBean bean;
        try
        {
            bean = new AttachmentRenderedJsonBean();
            bean.self = urls.restApi2BaseUrl() + "attachment/" + JiraUrlCodec.encode(attachment.getId().toString());
            bean.id = attachment.getId().toString();
            bean.filename = attachment.getFilename();
            bean.size = FileSize.format(attachment.getFilesize());
            bean.mimeType = attachment.getMimetype();
            bean.author = UserJsonBean.shortBean(attachment.getAuthorObject(), urls, loggedInUser, emailFormatter);
            bean.content = attachment.getFilename();
            bean.created = attachment.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(attachment.getCreated());

            String encodedFilename = URLEncoder.encode(attachment.getFilename(), "UTF-8");
            bean.content = String.format("%s/secure/attachment/%s/%s", urls.baseUrl(), attachment.getId(), encodedFilename);

            ThumbnailedImage thumbnail = thumbnailManager.toThumbnailedImage(thumbnailManager.getThumbnail(attachment.getIssueObject(), attachment));
            if (thumbnail != null)
            {
                bean.thumbnail = thumbnail.getImageURL();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error encoding file name", e);
        }

        return bean;
    }
}
