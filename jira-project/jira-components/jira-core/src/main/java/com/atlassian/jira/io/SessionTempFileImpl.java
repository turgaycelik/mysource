package com.atlassian.jira.io;

import java.io.File;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 6.0.8
 */
class SessionTempFileImpl implements SessionTempFile
{
    private final SessionTempFiles tempFiles;
    private final File file;

    public SessionTempFileImpl(SessionTempFiles tempFiles, File file)
    {
        this.tempFiles = checkNotNull(tempFiles);
        this.file = checkNotNull(file);
    }

    @Nonnull
    @Override
    public File getFile()
    {
        return file;
    }

    @Override
    public void unbind()
    {
        tempFiles.remove(this);
    }

    @Override
    public boolean delete()
    {
        boolean deleted = file.delete();
        if (deleted) { unbind(); }

        return deleted;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final SessionTempFileImpl that = (SessionTempFileImpl) o;

        if (!file.equals(that.file)) { return false; }
        if (!tempFiles.equals(that.tempFiles)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = tempFiles.hashCode();
        result = 31 * result + file.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("SessionTempFileImpl{session='%s', path='%s'}", tempFiles.getSessionId(), file.getAbsolutePath());
    }
}
