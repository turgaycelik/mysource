package com.atlassian.jira.io;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.InjectableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A SessionTempFile monitor for managing deletion of temporary files.
 *
 * @since 6.0.8
 */
@PublicApi
@InjectableComponent
public interface TempFileFactory
{
    /**
     * Makes a new SessionTempFile for the file at {@code path}. The lifecycle of the SessionTempFile will become bound
     * to the HTTP session associated with the current thread, meaning that the file will be deleted when the session is
     * destroyed.
     * <p/>
     * Note that for security reasons {@code path} must point to a file in {@code java.io.tmpdir} or in JIRA's temporary
     * attachments directory.
     *
     * @param path a relative (within {@code java.io.tmpdir}) or absolute path
     * @return a new SessionTempFile
     * @throws IllegalArgumentException if there is no file at {@code path} or if the file is not in a temporary file
     * directory
     * @throws SessionNotFoundException if there is no current session
     * @since 6.0.8
     */
    @Nonnull
    SessionTempFile makeSessionTempFile(String path) throws IllegalArgumentException, SessionNotFoundException;

    /**
     * Returns the SessionTempFile for the file at {@code path}, which must have been previously created using {@link
     * #makeSessionTempFile(String)}, or null if not found.
     *
     * @param path a relative (within {@code java.io.tmpdir}) or absolute path
     * @return a SessionTempFile or null if there is no SessionTempFile for {@code path}
     * @throws SessionNotFoundException if there is no current session
     * @since 6.0.8
     */
    @Nullable
    SessionTempFile getSessionTempFile(String path) throws SessionNotFoundException;
}
