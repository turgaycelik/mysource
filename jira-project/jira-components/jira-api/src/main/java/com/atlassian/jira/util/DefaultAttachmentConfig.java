package com.atlassian.jira.util;

import java.io.File;

public class DefaultAttachmentConfig implements AttachmentConfig
{
    @Override
    public File getTemporaryAttachmentDirectory()
    {
        return AttachmentUtils.getTemporaryAttachmentDirectory();
    }
}
