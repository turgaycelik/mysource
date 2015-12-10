package com.atlassian.jira.config.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to access the indexing configuration.
 *
 * @since v4.0
 */
public interface IndexingConfiguration
{
    int getIndexLockWaitTime();

    int getMaxReindexes();

    int getIssuesToForceOptimize();

    boolean isIndexAvailable();

    void disableIndex();

    void enableIndex();

    class PropertiesAdapter implements IndexingConfiguration
    {
        private volatile boolean indexAvailable = true;

        private static final class Defaults
        {
            private static final int MAX_REINDEXES = 4000;
            private static final int INDEX_LOCK_WAIT_TIME = 30000;
            private static final int ISSUES_TO_FORCE_OPTIMIZE = 400;
        }

        private final ApplicationProperties applicationProperties;

        public PropertiesAdapter(final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = notNull("applicationProperties", applicationProperties);
        }

        public int getIndexLockWaitTime()
        {
            return getIntProperty(applicationProperties, APKeys.JIRA_INDEX_LOCK_WAITTIME, Defaults.INDEX_LOCK_WAIT_TIME);
        }

        public int getMaxReindexes()
        {
            return getIntProperty(applicationProperties, APKeys.JIRA_MAX_REINDEXES, Defaults.MAX_REINDEXES);
        }

        public int getIssuesToForceOptimize()
        {
            return getIntProperty(applicationProperties, APKeys.JIRA_BULK_INDEX_UPDATE_OPTIMIZATION, Defaults.ISSUES_TO_FORCE_OPTIMIZE);
        }

        public boolean isIndexAvailable()
        {
            return indexAvailable;
        }

        public void enableIndex()
        {
            indexAvailable = true;
        }

        public void disableIndex()
        {
            indexAvailable = false;
        }
    }
}
