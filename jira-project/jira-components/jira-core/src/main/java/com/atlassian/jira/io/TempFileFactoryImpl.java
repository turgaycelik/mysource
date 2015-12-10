package com.atlassian.jira.io;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.AttachmentConfig;
import com.atlassian.jira.web.HttpServletVariables;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import static com.atlassian.jira.util.PathUtils.isPathInSecureDir;

/**
 * TempFileFactory implementation for managing deletion of temporary files.
 */
public class TempFileFactoryImpl implements TempFileFactory
{
    private final HttpServletVariables httpServletVariables;
    private final AttachmentConfig attachmentConfig;

    public TempFileFactoryImpl(HttpServletVariables httpServletVariables, final AttachmentConfig attachmentConfig)
    {
        this.httpServletVariables = httpServletVariables;
        this.attachmentConfig = attachmentConfig;
    }

    @Nonnull
    @Override
    public SessionTempFile makeSessionTempFile(final String path) throws IllegalArgumentException, IllegalStateException
    {
        final File file = checkFileExists(path);
        final HttpSession httpSession = httpServletVariables.getHttpSession();

        List<String> allowedDirs = getAllowedTempFileDirs();
        try
        {
            for (String dir : allowedDirs)
            {
                if (isPathInSecureDir(dir, file.getAbsolutePath()))
                {
                    return SessionTempFiles.forSession(httpSession).createTempFile(file);
                }
            }

            throw new IllegalArgumentException("Path is not in a temporary directory: " + path);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public SessionTempFile getSessionTempFile(final String path)
            throws IllegalArgumentException, SessionNotFoundException
    {
        final File file = findFile(path);
        final HttpSession httpSession = httpServletVariables.getHttpSession();

        return SessionTempFiles.forSession(httpSession).getTempFile(file);
    }

    private List<String> getAllowedTempFileDirs()
    {
        return ImmutableList.of(
                JiraSystemProperties.getInstance().getProperty("java.io.tmpdir"),
                attachmentConfig.getTemporaryAttachmentDirectory().getAbsolutePath()
        );
    }

    @Nonnull
    private File findFile(final String location)
    {
        File file = new File(location);
        if (!file.isAbsolute())
        {
            file = new File(JiraSystemProperties.getInstance().getProperty("java.io.tmpdir"), location);
        }

        return file;
    }

    @Nonnull
    private File checkFileExists(String location)
    {
        File file = findFile(location);
        if (!file.exists())
        {
            throw new IllegalArgumentException("File does not exist: " + location);
        }

        return file;
    }
}
