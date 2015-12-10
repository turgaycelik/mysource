package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;

/**
* @since v5.0
*/
@JsonIgnoreProperties (ignoreUnknown = true)
public class AttachmentJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String filename;

    @JsonProperty
    private UserJsonBean author;

    @XmlJavaTypeAdapter (Dates.DateTimeAdapter.class)
    private Date created;

    @JsonProperty
    private long size;

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

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public void setAuthor(UserJsonBean author)
    {
        this.author = author;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    /**
     * @deprecated Use {@link #shortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.issue.thumbnail.ThumbnailManager, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<AttachmentJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager)
    {
        return shortBeans(attachments, urls, thumbnailManager, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<AttachmentJsonBean> shortBeans(final Collection<Attachment> attachments, final JiraBaseUrls urls, final ThumbnailManager thumbnailManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Collection<AttachmentJsonBean> result = Lists.newArrayListWithCapacity(attachments.size());
        for (Attachment from : attachments)
        {
            result.add(shortBean(from, urls, thumbnailManager, loggedInUser, emailFormatter));
        }

        return result;
    }

    /**
     * @return null if the input is null
     * @deprecated Use {@link #shortBean(com.atlassian.jira.issue.attachment.Attachment, JiraBaseUrls, com.atlassian.jira.issue.thumbnail.ThumbnailManager, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static AttachmentJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager)
    {
        return shortBean(attachment, urls, thumbnailManager, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static AttachmentJsonBean shortBean(final Attachment attachment, final JiraBaseUrls urls, ThumbnailManager thumbnailManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (attachment == null)
        {
            return null;
        }
        final AttachmentJsonBean bean;
        try
        {
            bean = new AttachmentJsonBean();
            bean.self = urls.restApi2BaseUrl() + "attachment/" + JiraUrlCodec.encode(attachment.getId().toString());
            bean.id = attachment.getId().toString();
            bean.filename = attachment.getFilename();
            bean.size = attachment.getFilesize();
            bean.mimeType = attachment.getMimetype();
            ApplicationUser author = attachment.getAuthorObject();
            bean.author = UserJsonBean.shortBean(author, urls, loggedInUser, emailFormatter);
            bean.content = attachment.getFilename();
            bean.created = attachment.getCreated();

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
