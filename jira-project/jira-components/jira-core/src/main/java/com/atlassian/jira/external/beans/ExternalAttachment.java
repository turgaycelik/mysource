package com.atlassian.jira.external.beans;

import java.io.File;
import java.util.Date;

public class ExternalAttachment
{
    private String id;
    private String issueId;
    private String fileName;
    private Date attachedDate;
    private String attacher;
    private File attachedFile;

    public ExternalAttachment() {}

    public ExternalAttachment(final String id, final String issueId, final String fileName, final Date attachedDate, final String attacher)
    {
        this.id = id;
        this.issueId = issueId;
        this.fileName = fileName;
        this.attachedDate = attachedDate;
        this.attacher = attacher;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public Date getAttachedDate()
    {
        return attachedDate;
    }

    public void setAttachedDate(Date attachedDate)
    {
        this.attachedDate = attachedDate;
    }

    public String getAttacher()
    {
        return attacher;
    }

    public void setAttacher(String attacher)
    {
        this.attacher = attacher;
    }

    public File getAttachedFile()
    {
        return attachedFile;
    }

    public void setAttachedFile(File attachedFile)
    {
        this.attachedFile = attachedFile;
    }
}
