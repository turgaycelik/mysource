package com.atlassian.jira.index.ha;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.util.PathUtils;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.Logger;

/**
 * A service that when run will store a snapshot of the Lucene indexes
 *
 * @since v6.2
 */
public class IndexSnapshotService extends AbstractService
{
    private static final Logger LOG = Logger.getLogger(IndexSnapshotService.class);

    protected static final String SERVICE_NAME = "JIRA Index Snapshot Service";
    public static final String BACKUP_COUNT = "backupCount";
    public static final int DEFAULT_COUNT = 3;
    protected static final String DEFAULT_DATE_FORMAT = "yyyy-MMM-dd--HHmm";
    public static final String SERVICE_KEY = "indexsnapshotservice";

    private final IndexPathManager indexPathManager;
    private final JiraHome jiraHome;
    private final IndexUtils indexUtils;

    private int backupCount = DEFAULT_COUNT;

    public IndexSnapshotService(final IndexPathManager indexPathManager, final JiraHome jiraHome, final IndexUtils indexUtils)
    {
        this.indexPathManager = indexPathManager;
        this.jiraHome = jiraHome;
        this.indexUtils = indexUtils;
    }

    @Override
    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);

        if (hasProperty(BACKUP_COUNT))
        {
            String count = getProperty(BACKUP_COUNT);
            try
            {
                backupCount = Integer.parseInt(count);
                if (backupCount < 1)
                {
                    backupCount = DEFAULT_COUNT;
                    LOG.warn("Invalid Index backup count specified in service configuration; defaulting to " + DEFAULT_COUNT + ".");
                }
            }
            catch (NumberFormatException e)
            {
                LOG.warn("Invalid Index backup count specified in service configuration; defaulting to " + DEFAULT_COUNT + ".", e);
            }
        }
    }

    @Override
    public String getKey()
    {
        return SERVICE_KEY;
    }

    @Override
    public boolean isUnique()
    {
        return true;
    }

    @Override
    public boolean isInternal()
    {
        return true;
    }

    @Override
    public void run()
    {
        String destinationPath = PathUtils.joinPaths(jiraHome.getExportDirectory().getAbsolutePath(), "indexsnapshots");
        String id = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        indexUtils.takeIndexSnapshot(indexPathManager.getIndexRootPath(), destinationPath, id, backupCount);
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration(SERVICE_KEY, "services/com/atlassian/jira/service/services/index/indexsnapshotservice.xml", null);
    }

    public static String getServiceName()
    {
        return SERVICE_NAME;
    }
}
