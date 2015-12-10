package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;

/**
 * Update the file paths of default icons for Statuses, Issue Types, Priorities and Sub Tasks.
 * The default locations can be found in {@literal /jira-webapp/src/main/webapp/WEB-INF/iconimages.properties}.
 */
public class UpgradeTask_Build6007 extends UpgradeTask_Build843
{
    public UpgradeTask_Build6007(OfBizDelegator entityEngine, ConstantsManager constantsManager)
    {
        super(entityEngine, constantsManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "6007";
    }
}
