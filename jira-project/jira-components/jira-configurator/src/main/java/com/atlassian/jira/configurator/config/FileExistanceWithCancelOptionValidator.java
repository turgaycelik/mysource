package com.atlassian.jira.configurator.config;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class FileExistanceWithCancelOptionValidator extends Validator<String>
{
    private final FileSystem fileSystem;

    public FileExistanceWithCancelOptionValidator(@Nonnull final FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    @Override
    public String apply(@Nullable String label, @Nonnull String input) throws ValidationException
    {
        final String nonNullInput = Strings.nullToEmpty(input).trim();
        if (nonNullInput.isEmpty())
        {
            return null;
        }

        if (fileSystem.isFileExisting(nonNullInput))
        {
            return fileSystem.getAbsolutePath(nonNullInput);
        }
        else
        {
            throw new ValidationException(label, "The specified file doesn't exist or is not a file.");
        }
    }
}