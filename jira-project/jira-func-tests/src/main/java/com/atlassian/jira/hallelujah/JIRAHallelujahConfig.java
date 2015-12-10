package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.jms.JMSConfiguration;
import org.apache.commons.lang.StringUtils;

public class JIRAHallelujahConfig
{
    private static final String HALLELUJAH_QUEUE_ID_PROPERTY = "jira.hallelujah.queueId";

    public static JMSConfiguration getConfiguration()
    {
        JMSConfiguration configuration = JMSConfiguration.fromDefaultFile();
        final String queueId = System.getProperty(HALLELUJAH_QUEUE_ID_PROPERTY);
        if (StringUtils.isNotBlank(queueId))
        {
            String queuePrefix = configuration.getTestNameQueue();
            if (!StringUtils.isBlank(queuePrefix) && queuePrefix.endsWith("-name-queue"))
            {
                queuePrefix = queuePrefix.replace("-name-queue", "") + "-" + queueId;
            }
            else
            {
                queuePrefix = queueId;
            }
            configuration = JMSConfiguration.fromParams(configuration.getBrokerUrl(), queuePrefix, configuration.getId());
        }
        return configuration;
    }
}
