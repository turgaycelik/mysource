package com.atlassian.jira.web.action.admin.index;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.logging.QuietLogger;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
@SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
public class TestAbstractAsyncIndexerCommand
{
    @Test
    public void testCallJohnsonSetup() throws Exception
    {
        final AtomicBoolean doReindexCalled = new AtomicBoolean(false);
        final TestContainer johnsonEventContainer = new TestContainer();
        final Object o = new Object()
        {
            @SuppressWarnings("unused")
            public int size()
            {
                return 0;
            }
        };

        final IndexLifecycleManager issueIndexManager =
                (IndexLifecycleManager) DuckTypeProxy.getProxy(IndexLifecycleManager.class, o);
        final AbstractAsyncIndexerCommand command =
                new AbstractAsyncIndexerCommand(johnsonEventContainer, issueIndexManager,
                        QuietLogger.create(getClass().getName()), new MockI18nHelper(),
                        new MockI18nBean.MockI18nBeanFactory())
                {
                    @Override
                    public IndexCommandResult doReindex(final Context appEvent,
                                                        final IndexLifecycleManager indexManager)
                    {
                        doReindexCalled.set(true);
                        return new IndexCommandResult(42L);
                    }
                };
        command.setTaskProgressSink(TaskProgressSink.NULL_SINK);
        command.call();

        assertTrue("addCalled", johnsonEventContainer.isAddCalled());
        assertTrue("removeCalled", johnsonEventContainer.isRemoveCalled());
        assertTrue("doReindexCalled", doReindexCalled.get());

    }

    @Test
    public void testCallJohnsonTeardown() throws Exception
    {
        final AtomicBoolean doReindexCalled = new AtomicBoolean(false);
        final TestContainer johnsonEventContainer = new TestContainer();
        final Object o = new Object()
        {
            @SuppressWarnings("unused")
            public int size()
            {
                return 0;
            }
        };

        final IndexLifecycleManager issueIndexManager =
                (IndexLifecycleManager) DuckTypeProxy.getProxy(IndexLifecycleManager.class, o);
        final AbstractAsyncIndexerCommand command =
                new AbstractAsyncIndexerCommand(johnsonEventContainer, issueIndexManager,
                        QuietLogger.create(getClass().getName()), new MockI18nBean(),
                        new MockI18nBean.MockI18nBeanFactory())
                {
                    @Override
                    public IndexCommandResult doReindex(final Context appEvent,
                                                        final IndexLifecycleManager indexManager)
                    {
                        doReindexCalled.set(true);
                        throw new ObscureException(); //It should still tear down Johnson events
                    }
                };
        command.setTaskProgressSink(TaskProgressSink.NULL_SINK);

        try
        {
            command.call();
            fail("ObscureException should have been thrown!");
        }
        catch (final ObscureException ignore)
        {
        }

        assertTrue("addCalled", johnsonEventContainer.isAddCalled());
        assertTrue("removeCalled", johnsonEventContainer.isRemoveCalled());
        assertTrue("doReindexCalled", doReindexCalled.get());
    }

    static class ObscureException extends RuntimeException
    {
        private static final long serialVersionUID = -8890170084777229321L;

        @Override
        public Throwable fillInStackTrace()
        {
            return this;
        }
    }

    static class TestContainer extends JohnsonEventContainer
    {
        private final AtomicBoolean addCalled = new AtomicBoolean();
        private final AtomicBoolean removeCalled = new AtomicBoolean();

        @Override
        public void addEvent(final Event event)
        {
            assertNotNull(event);
            addCalled.set(true);
        }

        @Override
        public void removeEvent(final Event event)
        {
            assertNotNull(event);
            removeCalled.set(true);
        }

        public boolean isAddCalled()
        {
            return addCalled.get();
        }

        public boolean isRemoveCalled()
        {
            return removeCalled.get();
        }
    }
}
