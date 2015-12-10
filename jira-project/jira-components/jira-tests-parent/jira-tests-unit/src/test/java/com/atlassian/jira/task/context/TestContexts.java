package com.atlassian.jira.task.context;

import com.atlassian.jira.logging.QuietCountingLogger;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.johnson.event.Event;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
@SuppressWarnings("ConstantConditions")  // null checks
public class TestContexts
{
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullLogger()
    {
        Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(), null, "Test Message",
                "admin.indexing.percent.complete", "admin.indexing.current.index");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullI18n()
    {
        Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, null, Logger.getLogger(TestContexts.class),
                "Test Message", "admin.indexing.percent.complete", "admin.indexing.current.index");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullSink()
    {
        Contexts.percentageReporter(new Size(3), null, new MockI18nHelper(), Logger.getLogger(TestContexts.class),
                "Test Message", "admin.indexing.percent.complete", "admin.indexing.current.index");
    }

    @Test
    public void testCreateWithZeroTasks()
    {
        assertEquals(UnboundContext.class,
                Contexts.percentageReporter(new Size(0), TaskProgressSink.NULL_SINK, new MockI18nHelper(),
                        Logger.getLogger(TestContexts.class), "Test Message", "admin.indexing.percent.complete",
                        "admin.indexing.current.index").getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullEvent()
    {
        Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(),
                Logger.getLogger(TestContexts.class),
                "Test Message", "admin.indexing.percent.complete", "admin.indexing.current.index", (Event) null);
    }

    @Test
    public void testCreateWorksWithEvent()
    {
        Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(),
                Logger.getLogger(TestContexts.class),
                "Test Message", "admin.indexing.percent.complete", "admin.indexing.current.index",
                new MockJohnsonEvent());
    }

    @Test
    public void testSetProgress()
    {
        final QuietCountingLogger countLogger = QuietCountingLogger.create(getClass().getName());
        final CountedTaskProgressSink countedSink = new CountedTaskProgressSink();

        final Context context = Contexts.percentageReporter(new Size(10), countedSink, new MockI18nHelper(),
                countLogger, "Test Message", "admin.indexing.percent.complete",
                "admin.indexing.current.index", new MockJohnsonEvent());

        for (int cnt = 1; cnt <= 8; ++cnt)
        {
            context.start(null).complete();
            assertEquals(cnt, countedSink.getMakeProgressCount());
        }
    }

    private static class CountedTaskProgressSink implements TaskProgressSink
    {
        private int makeProgressCount = 0;

        public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
        {
            makeProgressCount++;
        }

        public int getMakeProgressCount()
        {
            return makeProgressCount;
        }
    }

    static class Size implements Sized
    {
        private final int size;

        public Size(final int size)
        {
            this.size = size;
        }

        public int size()
        {
            return size;
        }

        public boolean isEmpty()
        {
            return size == 0;
        }
    }
}
