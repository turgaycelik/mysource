package com.atlassian.jira.configurator.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class DefaultFileSystem implements FileSystem
{
    @Override
    public boolean isFileExisting(@Nullable final String fileName)
    {
        if (fileName != null)
        {
            final File file = new File(fileName);
            return file.exists() && file.isFile();
        }
        else
        {
            return false;
        }
    }

    @Override
    public String getAbsolutePath(@Nonnull final String fileName)
    {
        // TODO: should this return the canonical path instead?
        return new File(fileName).getAbsolutePath();
    }
}
