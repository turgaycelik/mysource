package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import org.apache.log4j.Logger;

/**
 * Synchronise any newly created remote directories and then
 * do a reindex of JIRA to ensure that index is up to date for JQL.
 *
 * @since v4.0
 */
public class UpgradeTask_Build603 extends AbstractReindexUpgradeTask
{
    private final static Logger LOG = Logger.getLogger(UpgradeTask_Build603.class);

    private final CrowdDirectoryService crowdDirectoryService;

    public UpgradeTask_Build603(CrowdDirectoryService crowdDirectoryService)
    {
        super();
        this.crowdDirectoryService = crowdDirectoryService;
    }

    @Override
    public String getBuildNumber()
    {
        return "603";
    }

  @Override
    public String getShortDescription()
    {
   return "Synchronising remote user directories and reindexing all data in JIRA.";
    }

 @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Retrieve all the directories and synchronize them if they are synchronizable.
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (crowdDirectoryService.isDirectorySynchronisable(directory.getId()))
            {
                LOG.debug("Reindex all data if indexing is turned on.");
                try
                {
                    crowdDirectoryService.synchroniseDirectory(directory.getId(), false);
                }
                catch (OperationFailedException e)
                {
                    LOG.warn("Directory - '" + directory.getName() + "' was not successfully synchronised."
                            + "  You might need to shutdown and restart JIRA after the upgrade completes.");
                }
            }
        }
        
        // Reindex
        super.doUpgrade(setupMode);
   }
}