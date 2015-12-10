package com.atlassian.jira.task.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.johnson.event.Event;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * Provides static methods for creating {@link Context} instances easily.
 *
 * @since v4.0
 */
@PublicApi
public class Contexts
{
    private static final Context NULL = new Context()
    {
        private final Task task = new Task()
        {
            public void complete()
            {
            }
        };

        public void setName(final String arg0)
        {
        }

        public Task start(final Object input)
        {
            return task;
        }

        @Override
        public int getNumberOfTasksToCompletion()
        {
            return 0;
        }
    };

    public static Context nullContext()
    {
        return NULL;
    }

    /**
     * @deprecated Since 6.3.6 use {@link #percentageReporter(com.atlassian.jira.util.collect.Sized,
     * com.atlassian.jira.task.TaskProgressSink, com.atlassian.jira.util.I18nHelper, org.apache.log4j.Logger, String,
     * String, String)}
     */
    @Deprecated
    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg)
    {
        return percentageReporter(sized, sink, i18n, logger, msg, "admin.indexing.percent.complete",
                "admin.indexing.current.index");
    }

    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final String uiMessageKeyPercentage,
                                             final String uiMessageKeyCurrent)
    {
        return createContext(sized, new CompositeSink(
                new TaskProgressPercentageContextSink(i18n, sink, uiMessageKeyPercentage, uiMessageKeyCurrent),
                new LoggingContextSink(logger, msg)));
    }

    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final String uiMessageKeyPercentage,
                                             final String uiMessageKeyCurrent, @Nonnull Level level)
    {
        return createContext(sized, new CompositeSink(
                new TaskProgressPercentageContextSink(i18n, sink, uiMessageKeyPercentage, uiMessageKeyCurrent),
                new LoggingContextSink(logger, msg, level)));
    }

    /**
     * @deprecated Since 6.3.6 use {@link #percentageReporter(com.atlassian.jira.util.collect.Sized,
     * com.atlassian.jira.task.TaskProgressSink, com.atlassian.jira.util.I18nHelper, org.apache.log4j.Logger, String,
     * String, String, com.atlassian.johnson.event.Event)}
     */
    @Deprecated
    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final Event event)
    {
        return percentageReporter(sized, sink, i18n, logger, msg, "admin.indexing.percent.complete",
                "admin.indexing.current.index", event);
    }

   /**
     * @deprecated Since 6.3.6 use {@link #percentageReporter(com.atlassian.jira.util.collect.Sized,
     * com.atlassian.jira.task.TaskProgressSink, com.atlassian.jira.util.I18nHelper, org.apache.log4j.Logger, String,
     * String, String, com.atlassian.johnson.event.Event), Level}
     */
    @Deprecated
    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final Event event, Level level)
    {
        return percentageReporter(sized, sink, i18n, logger, msg, "admin.indexing.percent.complete",
                "admin.indexing.current.index", event);
    }

    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final String uiMessageKeyPercentage,
                                             final String uiMessageKeyCurrent, @Nonnull final Event event)
    {
        return createContext(sized, new CompositeSink(new JohnsonEventSink(event),
                new TaskProgressPercentageContextSink(i18n, sink, uiMessageKeyPercentage, uiMessageKeyCurrent),
                new LoggingContextSink(logger, msg)));
    }

    public static Context percentageReporter(@Nonnull final Sized sized, @Nonnull final TaskProgressSink sink,
                                             @Nonnull final I18nHelper i18n, @Nonnull final Logger logger,
                                             @Nonnull final String msg, @Nonnull final String uiMessageKeyPercentage,
                                             final String uiMessageKeyCurrent, @Nonnull final Event event, Level level)
    {
        return createContext(sized, new CompositeSink(new JohnsonEventSink(event),
                new TaskProgressPercentageContextSink(i18n, sink, uiMessageKeyPercentage, uiMessageKeyCurrent),
                new LoggingContextSink(logger, msg, level)));
    }

    public static Context percentageLogger(@Nonnull final Sized sized, @Nonnull final Logger logger,
                                           @Nonnull final String msg)
    {
        return createContext(sized, new LoggingContextSink(logger, msg));
    }

    public static Context percentageLogger(@Nonnull final Sized sized, @Nonnull final Logger logger,
                                           @Nonnull final String msg, Level level)
    {
        return createContext(sized, new LoggingContextSink(logger, msg, level));
    }

    private static Context createContext(@Nonnull final Sized sized, @Nonnull final Sink contextSink)
    {
        Assertions.notNull("sized", sized);
        final int size = sized.size();
        if (size > 0)
        {
            return new PercentageContext(size, contextSink);
        }
        return new UnboundContext(contextSink);
    }

    private Contexts()
    {
        throw new AssertionError("cannot instantiate!");
    }
}
