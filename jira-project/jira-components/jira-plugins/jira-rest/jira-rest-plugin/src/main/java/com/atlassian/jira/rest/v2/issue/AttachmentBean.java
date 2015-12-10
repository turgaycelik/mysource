package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @since v4.2
 */
@XmlRootElement (name="attachment")
public class AttachmentBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private String filename;

    @XmlElement
    private UserBean author;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date created;

    @XmlElement
    private long size;

    @XmlElement
    private String mimeType;

    // HACK (LGM) changed to a HashMap from PropertySet in order to get doc auto-generation working
    @XmlElement
    private HashMap<String, Object> properties;

    @XmlElement
    private String content;

    @XmlElement
    private String thumbnail;

    /**
     * Prevent instantiation.
     */
    private AttachmentBean()
    {
        // empty - used via reflection
    }

    public AttachmentBean(URI self, String filename, UserBean author, Date created, long filesize, String mimeType, HashMap<String, Object> properties, String content, String thumbnail)
    {
        this.self = self;
        this.filename = filename;
        this.author = author;
        this.created = created;
        this.size = filesize;
        this.mimeType = mimeType;
        this.properties = properties;
        this.content = content;
        this.thumbnail = thumbnail;
    }

    static final AttachmentBean DOC_EXAMPLE;
    static final AttachmentBean DOC_EXAMPLE_2;
    static final List<AttachmentBean> DOC_EXAMPLE_LIST;
    static {
        try
        {
            DOC_EXAMPLE = new AttachmentBean();
            DOC_EXAMPLE.self = new URI("http://www.example.com/jira/rest/api/2.0/attachments/10000");
            DOC_EXAMPLE.filename = "picture.jpg";
            DOC_EXAMPLE.author = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.created = new Date();
            DOC_EXAMPLE.size = 23123L;
            DOC_EXAMPLE.mimeType = "image/jpeg";
            DOC_EXAMPLE.content = "http://www.example.com/jira/attachments/10000";
            DOC_EXAMPLE.thumbnail = "http://www.example.com/jira/secure/thumbnail/10000";

            DOC_EXAMPLE_2 = new AttachmentBean();
            DOC_EXAMPLE_2.self = new URI("http://www.example.com/jira/rest/api/2.0/attachments/10001");
            DOC_EXAMPLE_2.filename = "dbeuglog.txt";
            DOC_EXAMPLE_2.author = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE_2.created = new Date();
            DOC_EXAMPLE_2.size = 2460L;
            DOC_EXAMPLE_2.mimeType = "text/plain";
            DOC_EXAMPLE_2.content = "http://www.example.com/jira/attachments/10001";
            DOC_EXAMPLE_2.thumbnail = "http://www.example.com/jira/secure/thumbnail/10002";

            DOC_EXAMPLE_LIST = ImmutableList.of(DOC_EXAMPLE, DOC_EXAMPLE_2);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    URI getSelf()
    {
        return self;
    }

    String getFilename()
    {
        return filename;
    }

    UserBean getAuthor()
    {
        return author;
    }

    Date getCreated()
    {
        return created;
    }

    long getSize()
    {
        return size;
    }

    String getMimeType()
    {
        return mimeType;
    }

    HashMap<String, Object> getProperties()
    {
        return properties;
    }

    String getContent()
    {
        return content;
    }

    String getThumbnail()
    {
        return thumbnail;
    }
}
