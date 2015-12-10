package com.atlassian.jira.cluster.logging;

import javax.annotation.Nonnull;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.log.LogMarker;
import com.atlassian.util.profiling.UtilTimerStack;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * Cluster wide logging level manager
 *
 * @since v6.3
 */
public class ClusterLoggingManager implements LoggingManager, Startable
{
    private static final String CHANNEL_LEVEL = "Log Level";
    private static final String CHANNEL_MARK = "Log Mark";
    private static final String CHANNEL_PROFILE = "Profile";
    private final MessageHandlerService messageHandlerService;
    private final JiraProperties jiraSystemProperties;
    // Need to assign to a variable to keep from being GC'ed
    private final LoggingMessageConsumer messageConsumer = new LoggingMessageConsumer();

    public ClusterLoggingManager(final MessageHandlerService messageHandlerService, final JiraProperties jiraSystemProperties)
    {
        this.messageHandlerService = messageHandlerService;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public void start() throws Exception
    {
        messageHandlerService.registerListener(CHANNEL_LEVEL, messageConsumer);
        messageHandlerService.registerListener(CHANNEL_MARK, messageConsumer);
        messageHandlerService.registerListener(CHANNEL_PROFILE, messageConsumer);
    }

    @Override
    public void setLogLevel(@Nonnull final String loggerName, @Nonnull final String levelName)
    {
        notNull("loggerName", loggerName);
        notNull("levelName", levelName);

        getLogger(loggerName).setLevel(Level.toLevel(levelName));
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, new Message(CHANNEL_LEVEL, serializeAsString(loggerName, levelName)));
    }

    @Override
    public void setLogLevel(@Nonnull final Logger logger, @Nonnull final Level level)
    {
        notNull("logger", logger);
        notNull("level", level);

        logger.setLevel(level);
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, new Message(CHANNEL_LEVEL, serializeAsString(logger.getName(), level.toString())));
    }

    @Override
    public void markLogs(final String msg, final boolean rollOver)
    {
        mark(msg, rollOver);
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, new Message(CHANNEL_MARK, serializeAsString(msg, String.valueOf(rollOver))));
    }

    @Override
    public void enableProfiling()
    {
        jiraSystemProperties.setProperty(UtilTimerStack.MIN_TIME, "1");
        UtilTimerStack.setActive(true);
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, new Message(CHANNEL_PROFILE, String.valueOf(true)));
    }

    @Override
    public void disableProfiling()
    {
        UtilTimerStack.setActive(false);
        messageHandlerService.sendMessage(ClusterManager.ALL_NODES, new Message(CHANNEL_PROFILE, String.valueOf(false)));
    }

    private static void mark(final String msg, final boolean rollOver)
    {
        if (rollOver)
        {
            LogMarker.rolloverAndMark(msg);
        }
        else
        {
            LogMarker.markLogs(msg);
        }
    }

    private static Logger getLogger(final String loggerName)
    {
        return "root".equals(loggerName) ? Logger.getRootLogger() : Logger.getLogger(loggerName);
    }

    private String serializeAsString(final String logger, final String levelName)
    {
        return logger + "-" + levelName;
    }

    class LoggingMessageConsumer implements ClusterMessageConsumer
    {

        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (channel.equals(CHANNEL_LEVEL))
            {
                getLogger(getLoggerFromMessageInfo(message)).setLevel(Level.toLevel(getLevelFromMessageInfo(message)));
            }
            if (channel.equals(CHANNEL_MARK))
            {
                mark(getMarkFromMessageInfo(message), getRolloverFromMessageInfo(message));
            }
            if (channel.equals(CHANNEL_PROFILE))
            {
                setProfiling(message);
            }
        }

        private void setProfiling(final String message)
        {
            boolean active = Boolean.valueOf(message);
            if (active)
            {
                jiraSystemProperties.setProperty(UtilTimerStack.MIN_TIME, "1");
            }
            UtilTimerStack.setActive(active);
        }

        private String getLoggerFromMessageInfo(final String messageInfo)
        {
            return messageInfo.substring(0, messageInfo.lastIndexOf(('-')));
        }

        private String getLevelFromMessageInfo(final String messageInfo)
        {
            return messageInfo.substring(messageInfo.lastIndexOf("-") + 1);
        }

        private String getMarkFromMessageInfo(final String messageInfo)
        {
            return messageInfo.substring(0, messageInfo.lastIndexOf(('-')));
        }

        private boolean getRolloverFromMessageInfo(final String messageInfo)
        {
            return Boolean.valueOf(messageInfo.substring(messageInfo.lastIndexOf("-") + 1));
        }
    }
}
