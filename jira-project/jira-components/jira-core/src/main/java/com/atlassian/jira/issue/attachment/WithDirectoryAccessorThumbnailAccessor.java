package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import org.apache.commons.io.FileUtils;

public abstract class WithDirectoryAccessorThumbnailAccessor implements ThumbnailAccessor
{
    private final AttachmentDirectoryAccessor attachmentDirectoryAccessor;

    public WithDirectoryAccessorThumbnailAccessor(final AttachmentDirectoryAccessor ada)
    {
        attachmentDirectoryAccessor = ada;
    }

    @Override
    @Nonnull
    public final File getThumbnailDirectory(final @Nonnull Issue issue)
    {
        return attachmentDirectoryAccessor.getThumbnailDirectory(issue);
    }

    @Override
    public Promise<Void> deleteThumbnailDirectory(@Nonnull final Issue issue)
    {
        File thumbnailDirectory = attachmentDirectoryAccessor.getThumbnailDirectory(issue);
        try
        {
            FileUtils.deleteDirectory(thumbnailDirectory);
            return Promises.promise(null);
        }
        catch (IOException e)
        {
            return Promises.rejected(new AttachmentCleanupException(e));
        }

    }

    protected final AttachmentDirectoryAccessor getAttachmentDirectoryAccessor()
    {
        return attachmentDirectoryAccessor;
    }
}