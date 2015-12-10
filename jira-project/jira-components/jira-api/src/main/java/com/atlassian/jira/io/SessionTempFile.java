package com.atlassian.jira.io;

import com.atlassian.annotations.PublicApi;

import java.io.File;
import javax.annotation.Nonnull;

/**
 * Wrapper for a temporary file that is bound to the HTTP session.
 *
 * @since 6.0.8
 */
@PublicApi
public interface SessionTempFile
{
    /**
     * Returns the underlying File object.
     *
     * @return a File
     * @since 6.0.8
     */
    @Nonnull
    File getFile();

    /**
     * Unbinds this SessionTempFile from the current session. The underlying file will no longer be deleted when the
     * session is destroyed.
     * <p/>
     * Note that this does not delete the underlying File
     *
     * @since 6.0.8
     */
    void unbind();

    /**
     * Deletes the underlying File and unbinds this SessionTempFile from the current session.
     *
     * @return true if the underlying file has been deleted
     * @since 6.0.8
     */
    boolean delete();
}
