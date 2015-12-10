package com.atlassian.jira.configurator.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface FileSystem
{
    boolean isFileExisting(@Nullable String fileName);

    String getAbsolutePath(@Nonnull String fileName);
}
