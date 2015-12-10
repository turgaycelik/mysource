package com.atlassian.jira.web.action.admin.index;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.logging.QuietLogger;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestActivateAsyncIndexerCommand
{
    private static final int REINDEX_TIME = 567;

    @Test
    public void testActivateCommandWithoutDeactivate() throws Exception
    {
        final Logger log = Logger.getLogger(getClass());
        final MockI18nBean i18nHelper = new MockI18nBean();

        final AtomicReference<Context> eventRef = new AtomicReference<Context>();
        final Object duckIssueIndexManager = new Object()
        {
            @SuppressWarnings("unused")
            public long activate(final Context event) throws IndexException
            {
                eventRef.set(event);

                return REINDEX_TIME;
            }

            @SuppressWarnings("unused")
            public int size()
            {
                return 3;
            }
        };

        final IssueIndexManager mockIssueIndexManager =
                (IssueIndexManager) DuckTypeProxy.getProxy(IssueIndexManager.class, duckIssueIndexManager);

        final ActivateAsyncIndexerCommand activateAsyncIndexerCommand =
                new ActivateAsyncIndexerCommand(false, null, mockIssueIndexManager, log,
                        i18nHelper, new MockI18nBean.MockI18nBeanFactory());

        activateAsyncIndexerCommand.setTaskProgressSink(TaskProgressSink.NULL_SINK);
        final IndexCommandResult result = activateAsyncIndexerCommand.call();

        assertTrue(result.isSuccessful());
        assertNotNull(eventRef.get());
        assertEquals(REINDEX_TIME, result.getReindexTime());

    }

    @Test
    public void testActivateCommandWithDeactivate() throws Exception
    {
        final AtomicReference<Context> eventRef = new AtomicReference<Context>();
        final AtomicBoolean calledDeactivate = new AtomicBoolean(false);

        final Object duckIssueIndexManager = new Object()
        {
            @SuppressWarnings("unused")
            public long activate(final Context event) throws IndexException
            {
                eventRef.set(event);

                return REINDEX_TIME;
            }

            @SuppressWarnings("unused")
            public void deactivate() throws Exception
            {
                calledDeactivate.set(true);
            }

            @SuppressWarnings("unused")
            public int size()
            {
                return 3;
            }
        };

        final IssueIndexManager mockIssueIndexManager =
                (IssueIndexManager) DuckTypeProxy.getProxy(IssueIndexManager.class, duckIssueIndexManager);

        final Logger log = QuietLogger.create(getClass().getName());
        final MockI18nBean i18nHelper = new MockI18nBean();

        final ActivateAsyncIndexerCommand activateAsyncIndexerCommand =
                new ActivateAsyncIndexerCommand(true, null, mockIssueIndexManager, log,
                        i18nHelper, new MockI18nBean.MockI18nBeanFactory());
        activateAsyncIndexerCommand.setTaskProgressSink(TaskProgressSink.NULL_SINK);
        final IndexCommandResult result = activateAsyncIndexerCommand.call();

        assertTrue(result.isSuccessful());
        assertNotNull(eventRef.get());
        assertTrue(calledDeactivate.get());
        assertEquals(REINDEX_TIME, result.getReindexTime());
    }

    @Test
    public void testActivateWithError() throws Exception
    {
        final Object duckIssueIndexManager = new Object()
        {
            @SuppressWarnings("unused")
            public long activate(final Context event) throws IndexException
            {
                throw new IndexException("testActivateWithError.activate");
            }

            @SuppressWarnings("unused")
            public void deactivate() throws Exception
            {
                throw new IndexException("testActivateWithError.deactivate");
            }

            @SuppressWarnings("unused")
            public int size()
            {
                return 3;
            }
        };

        final Logger log = QuietLogger.create(getClass().getName());
        final MockI18nBean i18nHelper = new MockI18nBean();
        final IssueIndexManager mockIssueIndexManager =
                (IssueIndexManager) DuckTypeProxy.getProxy(IssueIndexManager.class, duckIssueIndexManager);

        final ActivateAsyncIndexerCommand activateAsyncIndexerCommand =
                new ActivateAsyncIndexerCommand(true, null, mockIssueIndexManager, log,
                        i18nHelper, new MockI18nBean.MockI18nBeanFactory());
        activateAsyncIndexerCommand.setTaskProgressSink(TaskProgressSink.NULL_SINK);
        final IndexCommandResult result = activateAsyncIndexerCommand.call();

        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorCollection().hasAnyErrors());
    }
}
