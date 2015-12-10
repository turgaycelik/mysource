package com.atlassian.jira.io;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.atlassian.jira.cluster.ClusterSafe;

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since 6.0.8
 */
@ClusterSafe("Only because we have session affinity")
class SessionTempFiles implements HttpSessionBindingListener
{
    /**
     * Name of the session attribute to use.
     */
    @VisibleForTesting
    static final String SESSION_ATTRIBUTE_NAME = "jira.SessionTempFiles";

    /**
     * Logger for SessionTempFiles.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SessionTempFiles.class);

    /**
     * Gets or creates the SessionTempFiles instance that is associated with an HttpSession.
     *
     * @param session an HttpSession
     * @return a SessionTempFiles
     */
    @Nonnull
    @SuppressWarnings({
            // Session attributes can be anything, so we have to cast it.
            "CastToConcreteClass",

            // Sessions don't have their own thread-safe methods, so we have to synchronize on it.
            "SynchronizationOnLocalVariableOrMethodParameter",

            // Our member fields are not serializable, and we do not support session serialization
            // at this time, so this is ok.
            "NonSerializableObjectBoundToHttpSession"
    })
    public static SessionTempFiles forSession(@Nonnull HttpSession session)
    {
        SessionTempFiles tempFiles = (SessionTempFiles)session.getAttribute(SESSION_ATTRIBUTE_NAME);
        if (tempFiles == null)
        {
            synchronized (session)
            {
                tempFiles = (SessionTempFiles)session.getAttribute(SESSION_ATTRIBUTE_NAME);
                if (tempFiles == null)
                {
                    tempFiles = new SessionTempFiles(session);
                    session.setAttribute(SESSION_ATTRIBUTE_NAME, tempFiles);
                }
            }
        }

        return tempFiles;
    }

    /**
     * The parent HttpSession's session ID.
     */
    private final String sessionId;

    /**
     * List of {@code SessionTempFile} instances associated to this session.
     */
    private final ConcurrentLinkedQueue<SessionTempFile> tempFiles = new ConcurrentLinkedQueue<SessionTempFile>();

    /**
     * Creates a new {@code SessionTempFiles} for holding a list of temporary files that are bound to this session.
     *
     * @param session the parent HttpSession
     */
    @VisibleForTesting
    SessionTempFiles(@Nonnull HttpSession session)
    {
        notNull("session", session);
        this.sessionId = notNull("sessionId", session.getId());
    }

    @Override
    public void valueBound(final HttpSessionBindingEvent event)
    {
        // do nothing
    }

    @Override
    public void valueUnbound(final HttpSessionBindingEvent event)
    {
        try
        {
            deleteAllSessionTempFiles();
        }
        catch (RuntimeException e)
        {
            // don't rethrow as this would break JIRA logout
            LOG.error("Error deleting session temp files for session '" + sessionId + '\'', e);
        }
    }

    /**
     * Returns the id of the session where this SessionTempFiles is stored.
     *
     * @return the id of the session where this SessionTempFiles is stored
     */
    String getSessionId()
    {
        return sessionId;
    }

    /**
     * Creates a new SessionTempFile.
     *
     * @param file a File
     * @return a new SessionTempFile
     */
    SessionTempFile createTempFile(File file)
    {
        SessionTempFileImpl sessionTempFile = new SessionTempFileImpl(this, file);
        tempFiles.add(sessionTempFile);
        return sessionTempFile;
    }

    /**
     * Gets the existing {@code SessionTempFile} container for the specified temporary file.
     *
     * @param file the temporary file
     * @return the existing {@code SessionTempFile} container, or {@code null} if the specified file
     *      is not bound to the session
     */
    @Nullable
    SessionTempFile getTempFile(File file)
    {
        for (SessionTempFile tempFile : tempFiles)
        {
            if (tempFile.getFile().equals(file))
            {
                return tempFile;
            }
        }

        return null;
    }

    /**
     * Unregisters the specified {@code SessionTempFile} <strong>without deleting it</strong>.
     *
     * @param sessionTempFile the session temp file to unbind from the session.
     */
    void remove(SessionTempFile sessionTempFile)
    {
        tempFiles.remove(sessionTempFile);
    }

    private void deleteAllSessionTempFiles()
    {
        final Iterator<SessionTempFile> iter = tempFiles.iterator();
        while (iter.hasNext())
        {
            final SessionTempFile tempFile = iter.next();
            iter.remove();

            final File file = tempFile.getFile();
            if (!file.delete() && file.exists())
            {
                LOG.warn("Failed to delete {}. Marking this file for deletion with File.deleteOnExit()...", tempFile);
                file.deleteOnExit();
            }
        }
    }
}
