package com.atlassian.jira.web.util;

/**
 * A nice injectable util for retreiving an icon for file.
 *
 * @since v5.0
 */
public interface FileIconUtil
{
    /**
     * Get the icon for file
     *
     * @param fileName the name of the file
     * @param mimeType the mimetype of the file
     * @return object representing the icon
     */
    public FileIconBean.FileIcon getFileIcon(final String fileName, final String mimeType);

}
