package com.atlassian.jira.logging;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.startup.JiraHomeStartupCheck;

import com.google.common.collect.Lists;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
public class TestJiraHomeAppender
{
    private static final Logger log = Logger.getLogger(TestJiraHomeAppender.class);
    private static final String LOGGER_FILE = "logger.test.log";
    private static final int MAX_QUEUED_EVENTS = 100;

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule public InitMockitoMocks mocks = new InitMockitoMocks(this);

    @Mock
    private JiraHomeStartupCheck check;
    private JiraHomeAppender appender;
    private TestFilter filter = new TestFilter();

    @Before
    public void init()
    {
        final File jiraHome = new File(temporaryFolder.getRoot(), "home").getAbsoluteFile();
        assertThat(jiraHome.mkdir(), Matchers.is(true));

        appender = new JiraHomeAppender(check);
        appender.addFilter(filter);
        appender.setFile(LOGGER_FILE);

        when(check.isInitialised()).thenReturn(true);
        when(check.isOk()).thenReturn(true);
        when(check.getJiraHomeDirectory()).thenReturn(jiraHome);
    }

    /**
     * Test when the appender is disabled.
     */
    @Test
    public void loggerOff()
    {
        appender.setThreshold(Level.OFF);
        appender.doAppend(msg("Ignore"));

        assertThat(filter.getMessages(), IsEmptyCollection.<String>empty());
        assertThat(files(temporaryFolder.getRoot()), Matchers.<String>empty());
    }

    /**
     * Test when the appender is used before JIRA.HOME is set.
     */
    @Test
    public void loggerDelayedLog()
    {
        when(check.isInitialised()).thenReturn(false);

        appender.setThreshold(Level.ERROR);
        appender.doAppend(msg("One"));

        assertThat(filter.getMessages(), IsEmptyCollection.<String>empty());
        assertThat(files(temporaryFolder.getRoot()), Matchers.<String>empty());

        when(check.isInitialised()).thenReturn(true);

        appender.doAppend(msg("Two"));

        assertThat(filter.getMessages(), Matchers.contains("One", "Two"));
        assertThat(files(temporaryFolder.getRoot()), Matchers.contains("home/log/logger.test.log"));
    }

    /**
     * Test when the appender is configured with a bad JIRA.HOME.
     */
    @Test
    public void loggerLogBadJiraHome()
    {
        when(check.isOk()).thenReturn(false);

        appender.setFile(new File(temporaryFolder.getRoot(), "bad.log").getAbsolutePath());

        appender.setThreshold(Level.ERROR);
        appender.doAppend(msg("One"));

        assertThat(filter.getMessages(), Matchers.contains("One"));
        assertThat(files(temporaryFolder.getRoot()), Matchers.contains("bad.log"));
    }

    /**
     * Test when the event buffer overflows when buffering events before JIRA.HOME set.
     */
    @Test
    public void loggerDelayedLogFull()
    {
        when(check.isInitialised()).thenReturn(false);

        appender.setThreshold(Level.ERROR);

        final List<String> actualMessages = getStrings(MAX_QUEUED_EVENTS + 1);
        for (String logMessage : actualMessages)
        {
            appender.doAppend(msg(logMessage));
        }

        assertThat(filter.getMessages(), IsEmptyCollection.<String>empty());
        assertThat(files(temporaryFolder.getRoot()), Matchers.<String>empty());

        when(check.isInitialised()).thenReturn(true);

        appender.doAppend(msg("TooMany"));

        final List<String> messages = filter.getMessages();
        assertThat(messages.subList(0, MAX_QUEUED_EVENTS), Matchers.is(actualMessages.subList(0, MAX_QUEUED_EVENTS)));
        assertThat(messages.get(MAX_QUEUED_EVENTS), Matchers.containsString("Some log messages dropped during startup."));
        assertThat(messages.get(MAX_QUEUED_EVENTS + 1), Matchers.containsString("TooMany"));
        assertThat(files(temporaryFolder.getRoot()), Matchers.contains("home/log/logger.test.log"));
    }

    private static List<String> getStrings(final int count)
    {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < count; i++)
        {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private List<String> files(File directory)
    {
        return files(directory, null);
    }

    private List<String> files(File directory, String path)
    {
        List<String> names = Lists.newArrayList();
        final File[] files = directory.listFiles();
        if (files == null)
        {
            return Collections.emptyList();
        }
        for (File file : files)
        {
            final String childPath;
            if (path == null)
            {
                childPath = file.getName();
            }
            else
            {
                childPath = path + "/" + file.getName();
            }
            if (file.isFile())
            {
                names.add(childPath);
            }
            else if (file.isDirectory())
            {
                names.addAll(files(file, childPath));
            }
        }
        return names;
    }

    private static LoggingEvent msg(String msg)
    {
        return new LoggingEvent(Level.class.getName(), log, Level.ERROR, msg, null);
    }

    private static class TestFilter extends Filter
    {
        private List<String> messages = Lists.newArrayList();

        @Override
        public int decide(final LoggingEvent event)
        {
            messages.add(event.getMessage().toString());
            return Filter.DENY;
        }

        public List<String> getMessages()
        {
            return messages;
        }
    }
}
