package com.atlassian.jira.cluster.logging;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.util.profiling.UtilTimerStack;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TestClusterLoggingManager
{
    @Mock
    private MessageHandlerService messageHandlerService;
    @Mock
    private JiraProperties jiraProperties;
    private ClusterLoggingManager clusterLoggingManager;
    private ClusterLoggingManager.LoggingMessageConsumer messageConsumer;

    @Before
    public void setup() throws Exception
    {
        ArgumentCaptor<ClusterLoggingManager.LoggingMessageConsumer> argument = ArgumentCaptor.forClass(ClusterLoggingManager.LoggingMessageConsumer.class);
        clusterLoggingManager = new ClusterLoggingManager(messageHandlerService, jiraProperties);
        clusterLoggingManager.start();
        verify(messageHandlerService).registerListener(eq("Log Level"), argument.capture());
        messageConsumer = argument.getValue();
    }

    @Test
    public void testSetLogLevel() throws Exception
    {

        clusterLoggingManager.setLogLevel("com.atlassian.jira.test.xxx", "DEBUG");
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Log Level", "com.atlassian.jira.test.xxx-DEBUG"));
        assertThat(Logger.getLogger("com.atlassian.jira.test.xxx").getLevel(), is(Level.DEBUG));
    }

    @Test
    public void testSetLogLevelObjects() throws Exception
    {
        Logger aLogger = Logger.getLogger("com.atlassian.jira.test.xxx");
        clusterLoggingManager.setLogLevel(aLogger, Level.DEBUG);
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Log Level", "com.atlassian.jira.test.xxx-DEBUG"));
        assertThat(Logger.getLogger("com.atlassian.jira.test.xxx").getLevel(), is(Level.DEBUG));
    }

    @Test
    public void testReceiveSetLogLevel() throws Exception
    {
        messageConsumer.receive("Log Level", "com.atlassian.jira.test.xxx-DEBUG", "XXX");
        assertThat(Logger.getLogger("com.atlassian.jira.test.xxx").getLevel(), is(Level.DEBUG));
        messageConsumer.receive("Log Level", "com.atlassian.jira.test.yyy-INFO", "XXX");
        assertThat(Logger.getLogger("com.atlassian.jira.test.yyy").getLevel(), is(Level.INFO));
    }

    @Test
    public void testMarkLog() throws Exception
    {
        clusterLoggingManager.markLogs("A New Beginning", true);
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Log Mark", "A New Beginning-true"));
        clusterLoggingManager.markLogs("A New Beginning", false);
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Log Mark", "A New Beginning-false"));
    }

    @Test
    public void testReceiveMarkLog() throws Exception
    {
        messageConsumer.receive("Log Mark", "A New Beginning-true", "XXX");
    }

    @Test
    public void testEnableProfiling() throws Exception
    {
        clusterLoggingManager.enableProfiling();
        verify(jiraProperties).setProperty(UtilTimerStack.MIN_TIME, "1");
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Profile", "true"));
        assertTrue(UtilTimerStack.isActive());
    }

    @Test
    public void testReceiveEnableProfiling() throws Exception
    {
        messageConsumer.receive("Profile", "true", "XXX");
        verify(jiraProperties).setProperty(UtilTimerStack.MIN_TIME, "1");
        assertTrue(UtilTimerStack.isActive());
    }

    @Test
    public void testDisableProfiling() throws Exception
    {
        clusterLoggingManager.disableProfiling();
        verify(jiraProperties, never()).setProperty(UtilTimerStack.MIN_TIME, "1");
        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message("Profile", "false"));
        assertFalse(UtilTimerStack.isActive());
    }

    @Test
    public void testReceiveDisableProfiling() throws Exception
    {
        messageConsumer.receive("Profile", "false", "XXX");
        verify(jiraProperties, never()).setProperty(UtilTimerStack.MIN_TIME, "1");
        assertFalse(UtilTimerStack.isActive());
    }

}
