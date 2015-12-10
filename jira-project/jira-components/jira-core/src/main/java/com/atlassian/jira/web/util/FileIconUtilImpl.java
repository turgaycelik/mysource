package com.atlassian.jira.web.util;

/**
 * Default implementation
 *
 * @since v5.0
 */
public class FileIconUtilImpl implements FileIconUtil
{
    private final FileIconBean fileIconBean;

    public FileIconUtilImpl(FileIconBean fileIconBean)
    {
        this.fileIconBean = fileIconBean;
    }

    @Override
    public FileIconBean.FileIcon getFileIcon(String fileName, String mimeType)
    {
        return fileIconBean.getFileIcon(fileName, mimeType);
    }
}
