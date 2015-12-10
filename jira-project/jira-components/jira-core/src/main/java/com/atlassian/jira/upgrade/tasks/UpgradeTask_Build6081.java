package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;

/**
 * Update the file paths of default icons for Statuses, Issue Types, Priorities and Sub Tasks.
 * The default locations can be found in {@literal /jira-webapp/src/main/webapp/WEB-INF/iconimages.properties}.
 * JRADEV-19766, previous upgrade tasks did not run during setup, so need this upgrade task to ensure we cater
 * for those instances that have been setup between 5.2.2 and 6.0 release
 */
public class UpgradeTask_Build6081 extends UpgradeTask_Build843
{
    public UpgradeTask_Build6081(OfBizDelegator entityEngine, ConstantsManager constantsManager)
    {
        super(entityEngine, constantsManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "6081";
    }
}
