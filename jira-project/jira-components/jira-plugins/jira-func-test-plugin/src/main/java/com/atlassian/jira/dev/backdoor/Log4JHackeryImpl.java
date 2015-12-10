package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.dataimport.DefaultDataImportService;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.upgrade.ConsistencyCheckImpl;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.web.action.admin.index.IndexAdminImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @since v4.4
 */
public class Log4JHackeryImpl implements Log4JHackery, Startable
{
    @Override
    public void start() throws Exception
    {
        Logger.getLogger(DefaultDataImportService.class).setLevel(Level.ERROR);
        Logger.getLogger(UpgradeManagerImpl.class).setLevel(Level.ERROR);
        Logger.getLogger(DefaultIndexManager.class).setLevel(Level.ERROR);
        Logger.getLogger(ConsistencyCheckImpl.class).setLevel(Level.ERROR);
        Logger.getLogger(IndexAdminImpl.class).setLevel(Level.ERROR);
        Logger.getLogger("com.atlassian.jira.upgrade.tasks").setLevel(Level.ERROR);
    }
}
