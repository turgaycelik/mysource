package com.atlassian.jira.config.database;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;

/**
 * Class that loads and persists database configuration by loading from and saving to {@link #FILENAME_DBCONFIG} in the
 * JIRA home directory. This implementation is no longer tenant aware
 *
 * @since v4.4
 */
public class SystemDatabaseConfigurationLoader extends AbstractJiraHomeDatabaseConfigurationLoader
{
    private static final Logger log = Logger.getLogger(SystemDatabaseConfigurationLoader.class);

    @ClusterSafe
    private LazyReference<String> jiraHomePathRef;

    public SystemDatabaseConfigurationLoader(final JiraHome jiraHome)
    {
        this.jiraHomePathRef = new LazyReference<String>()
        {
            @Override
            protected String create()
            {
                return jiraHome.getLocalHomePath();
            }
        };
    }

    @Override
    protected String getJiraHome()
    {
        try
        {
            return jiraHomePathRef.get();
        }
        catch (LazyReference.InitializationException e)
        {
            /* LazyReference.get() wraps the original exception that was thrown in
             * LazyReference.create() method inside LazyReference.InitializationException (see its javadoc)
             * We know that our create method(defined above) does not throw any checked exceptions and
             * it is safe to cast to RuntimeException
             */
            throw (RuntimeException) e.getCause();
        }
    }

    @Override
    protected void logInfo(String message)
    {
        log.info(message);
    }
}
