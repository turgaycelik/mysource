package com.atlassian.jira.web.action.admin.index;

import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** @since v3.13 */
public class TestReIndexAsyncIndexerCommand
{
    private static final int REINDEX_TIME = 567;
    private static final String TEST_MESSAGE = "TestMessage";

    @Test
    public void testReindexOK() throws Exception
    {
        final Logger log = Logger.getLogger(getClass());
        final MockI18nBean i18nHelper = new MockI18nBean();
        final AtomicReference<Context> eventRef = new AtomicReference<Context>();

        final Object duckIssueIndexManager = new Object()
        {
            @SuppressWarnings("unused")
            public long reIndexAll(final Context context) throws IndexException
            {
                eventRef.set(context);
                return REINDEX_TIME;
            }

            @SuppressWarnings("unused")
            public int size()
            {
                return 3;
            }
        };

        final IssueIndexManager mockIssueIndexManager = (IssueIndexManager) DuckTypeProxy.getProxy(IssueIndexManager.class, duckIssueIndexManager);

        final ReIndexAsyncIndexerCommand reIndexAsyncIndexerCommand = new ReIndexAsyncIndexerCommand(null, mockIssueIndexManager, log, i18nHelper,  new MockI18nBean.MockI18nBeanFactory());
        reIndexAsyncIndexerCommand.setTaskProgressSink(TaskProgressSink.NULL_SINK);
        final IndexCommandResult result = reIndexAsyncIndexerCommand.call();

        assertTrue(result.isSuccessful());
        assertNotNull(eventRef.get());
        assertEquals(REINDEX_TIME, result.getReindexTime());
    }
}
