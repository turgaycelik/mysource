package com.atlassian.jira.util;

import java.io.File;

/**
 * @since 6.0.8
 */
public class MockAttachmentConfig implements AttachmentConfig
{
    private File temporaryAttachmentDirectory;

    public MockAttachmentConfig()
    {
        temporaryAttachmentDirectory = TempDirectoryUtil.createTempDirectory("tmp_attachments");
        temporaryAttachmentDirectory.deleteOnExit();
    }

    @Override
    public File getTemporaryAttachmentDirectory()
    {
        return temporaryAttachmentDirectory;
    }

    public void setTemporaryAttachmentDirectory(final File temporaryAttachmentDirectory)
    {
        this.temporaryAttachmentDirectory = temporaryAttachmentDirectory;
    }
}
