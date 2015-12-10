package com.atlassian.jira.config.util;

/**
 * @since v4.0
 */
public class MockAttachmentPathManager implements AttachmentPathManager
{
    public static final String DEFAULT_PATH = "/a/p/a/t/h";
    private String path = DEFAULT_PATH;
    private boolean useDefaultDirectory = false;
    private boolean attachmentsDisabled = false;

    public MockAttachmentPathManager() {}

    public MockAttachmentPathManager(final String path)
    {
        setCustomAttachmentPath(path);
    }

    public String getDefaultAttachmentPath()
    {
        return "/jira-home/data/attachments";
    }

    public String getAttachmentPath()
    {
        return path;
    }

    public void setCustomAttachmentPath(final String indexPath)
    {
        path = indexPath;
        useDefaultDirectory = false;
    }

    public void setUseDefaultDirectory()
    {
        useDefaultDirectory = true;
    }

    public boolean getUseDefaultDirectory()
    {
        return useDefaultDirectory;
    }

    public void disableAttachments()
    {
        attachmentsDisabled = true;
    }

    public Mode getMode()
    {
        if (attachmentsDisabled)
        {
            return AttachmentPathManager.Mode.DISABLED;
        }
        if(useDefaultDirectory)
        {
            return AttachmentPathManager.Mode.DEFAULT;
        }
        else
        {
            return AttachmentPathManager.Mode.CUSTOM;
        }
    }
}
