package com.atlassian.jira.util;

import java.io.File;

/**
 * Interface getting attachment configurations.
 *
 * @since 6.0.8
 */
public interface AttachmentConfig
{
    /**
     * Returns a File object for the temporary attachments directory.
     *
     * @return a File object for the temporary attachments directory
     */
    File getTemporaryAttachmentDirectory();
}
