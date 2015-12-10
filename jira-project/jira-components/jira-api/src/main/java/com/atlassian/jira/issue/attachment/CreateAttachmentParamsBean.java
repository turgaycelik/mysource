package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Preconditions;
import org.ofbiz.core.util.UtilDateTime;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * Bean containing parameters, which describe created attachment.
 * @since v6.0
 */
@PublicApi
public class CreateAttachmentParamsBean
{
    private final File file;
    private final String filename;
    private final String contentType;
    private final ApplicationUser author;
    private final Issue issue;
    private final Boolean zip;
    private final Boolean thumbnailable;
    private final Map<String, Object> attachmentProperties;
    private final Date createdTime;
    private final Boolean copySourceFile;

    /**
     * @param file A file on a locally accessible filesystem
     * @param filename The desired filename for this attachment.  This may be different to the filename on disk (for
     * example with temp files used in file uploads)
     * @param contentType The desired contentType.  Implementations of this interface can choose to override this value
     * as appropriate
     * @param author The user who created this attachment
     * @param issue The issue that this file is to be attached to
     * @param zip This file is a zip file.  Null indicates that it is not know if this attachment is a zip file or not
     * @param thumbnailable This file is thumbnailable (e.g. a png image).  Null indicates that it is not know if this
     * attachment is thumbnailable or not
     * @param attachmentProperties Attachment properties (a Map of String -> Object properties).  These are optional,
     * and are used to populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.
     * Pass null to set no properties
     * @param createdTime The created time
     * @param copySourceFile The source file should remain in file system. Null or false indicates that should be
     * removed.
     */
    public CreateAttachmentParamsBean(File file, String filename, String contentType, ApplicationUser author, Issue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime, Boolean copySourceFile)
    {
        this.file = file;
        this.filename = filename;
        this.contentType = contentType;
        this.author = author;
        this.issue = issue;
        this.zip = zip;
        this.thumbnailable = thumbnailable;
        this.attachmentProperties = attachmentProperties;
        this.createdTime = createdTime;
        this.copySourceFile = copySourceFile;
    }

    /*


    /**
     * @return A file on a locally accessible filesystem
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return The desired filename for this attachment.  This may be different to the filename on disk (for example
     *         with temp files used in file uploads)
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @return The desired contentType. Implementations of this interface can choose to override this value as
     *         appropriate
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @return The user who created this attachment
     */
    public ApplicationUser getAuthor()
    {
        return author;
    }

    /**
     * @return The issue that this file is to be attached to
     */
    public Issue getIssue()
    {
        return issue;
    }

    /**
     * @return This file is a zip file.  Null indicates that it is not know if this attachment is a zip file or not
     */
    public Boolean getZip()
    {
        return zip;
    }

    /**
     * @return This file is thumbnailable (e.g. a png image).  Null indicates that it is not know if this attachment is
     *         thumbnailable or not
     */
    public Boolean getThumbnailable()
    {
        return thumbnailable;
    }

    /**
     * @return Attachment properties (a Map of String -> Object properties).  These are optional, and are used to
     *         populate a PropertySet on the Attachment ({@link com.atlassian.jira.issue.attachment.Attachment#getProperties()}.
     *         Pass null to set no properties
     */
    public Map<String, Object> getAttachmentProperties()
    {
        return attachmentProperties;
    }

    /**
     * @return the created time
     */
    public Date getCreatedTime()
    {
        return createdTime;
    }

    /**
     * @return The source file should remain in file system. Null or false indicates that should be removed.
     */
    public Boolean getCopySourceFile()
    {
        return copySourceFile;
    }

    public static class Builder
    {
        private File file;
        private String filename;
        private String contentType;
        private ApplicationUser author;
        private Issue issue;
        private Boolean zip;
        private Boolean thumbnailable;
        private Map<String, Object> attachmentProperties;
        private Date createdTime = UtilDateTime.nowTimestamp();
        private Boolean copySourceFile = false;

        public Builder()
        {

        }

        public Builder(File file, String filename, String contentType, ApplicationUser author, Issue issue)
        {
            this.file = file;
            this.filename = filename;
            this.contentType = contentType;
            this.author = author;
            this.issue = issue;
        }

        public Builder file(File file)
        {
            this.file = file;
            return this;
        }

        public Builder filename(String filename)
        {
            this.filename = filename;
            return this;
        }

        public Builder contentType(String contentType)
        {
            this.contentType = contentType;
            return this;
        }

        public Builder author(ApplicationUser author)
        {
            this.author = author;
            return this;
        }

        public Builder issue(Issue issue)
        {
            this.issue = issue;
            return this;
        }

        public Builder zip(Boolean zip)
        {
            this.zip = zip;
            return this;
        }

        public Builder thumbnailable(Boolean thumbnailable)
        {
            this.thumbnailable = thumbnailable;
            return this;
        }

        public Builder attachmentProperties(Map<String, Object> attachmentProperties)
        {
            this.attachmentProperties = attachmentProperties;
            return this;
        }

        public Builder createdTime(Date createdTime)
        {
            this.createdTime = createdTime;
            return this;
        }

        public Builder copySourceFile(Boolean copySourceFile)
        {
            this.copySourceFile = copySourceFile;
            return this;
        }

        public CreateAttachmentParamsBean build()
        {
            Preconditions.checkNotNull(file);
            Preconditions.checkNotNull(filename);
            Preconditions.checkNotNull(author);
            Preconditions.checkNotNull(issue);
            return new CreateAttachmentParamsBean(file, filename, contentType, author, issue, zip, thumbnailable, attachmentProperties, createdTime, copySourceFile);
        }


    }
}
