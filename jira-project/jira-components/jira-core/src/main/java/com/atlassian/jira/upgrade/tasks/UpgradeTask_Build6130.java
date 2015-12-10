package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Adds keys to Project Key table.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6130 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6130.class);

    private static final String PROJECT_KEY_ENTITY_NAME = "ProjectKey";
    private static final String PROJECT_ENTITY_NAME = "Project";
    private final ProjectManager projectManager;

    public UpgradeTask_Build6130(ProjectManager projectManager)
    {
        super(false);
        this.projectManager = projectManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        //we have to remove all ProjectKey entities that could have been imported during downgrade from OnDemand
        getOfBizDelegator().removeAll(getOfBizDelegator().findAll(PROJECT_KEY_ENTITY_NAME));

        final List<GenericValue> projects = getOfBizDelegator().findAll(PROJECT_ENTITY_NAME);
        try
        {
            for (GenericValue project : projects)
            {
                getOfBizDelegator().createValue(PROJECT_KEY_ENTITY_NAME,
                        ImmutableMap.<String, Object>of("projectId", project.getLong("id"),
                                "projectKey", project.getString("key")));
            }
        }
        catch (DataAccessException e)
        {
            log.error("Update failed. One of possible reasons might be violation of unique constraint on PROJECT_ID "
                    + "column of PROJECT_KEY table. You can read more here: "
                    + "https://confluence.atlassian.com/display/JIRAKB/Upgrading+to+JIRA+6.1+Fails+Due+To+Duplicate+Project+Keys");
            throw e;
        }
        projectManager.refresh();
    }

    @Override
    public String getBuildNumber()
    {
        return "6130";
    }

    @Override
    public String getShortDescription()
    {
        return "Create table tracking historical Project keys";
    }
}
