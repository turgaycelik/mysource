package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.UpgradeTask;

import java.util.Collection;
import java.util.Collections;

/**
 * Updates user searchers to be user/group searchers and single select searchers to multi-select searchers
 *
 * @since v5.2
 */
public class UpgradeTask_Build809 implements UpgradeTask
{
    private static final String USER_GROUP_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:userpickergroupsearcher";
    private static final String USER_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:userpickersearcher";
    private static final String SINGLE_SELECT_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher";
    private static final String MULTI_SELECT_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher";
    private static final String FIELD_NAME = "customfieldsearcherkey";

    private final EntityEngine entityEngine;

    public UpgradeTask_Build809(EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "809";
    }

    @Override
    public String getShortDescription()
    {
        return "Updating User Searchers to User/Group Searchers and single-select searcher to be multi-select searchers";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        entityEngine.execute(Update.into("CustomField").set(FIELD_NAME, USER_GROUP_SEARCHER).whereEqual(FIELD_NAME, USER_SEARCHER));
        entityEngine.execute(Update.into("CustomField").set(FIELD_NAME, MULTI_SELECT_SEARCHER).whereEqual(FIELD_NAME, SINGLE_SELECT_SEARCHER));
    }

    @Override
    public Collection<String> getErrors()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isReindexRequired()
    {
        return false;
    }
}
