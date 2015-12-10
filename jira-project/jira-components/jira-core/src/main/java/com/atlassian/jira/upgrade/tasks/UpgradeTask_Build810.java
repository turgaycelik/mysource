package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.UpgradeTask;

import java.util.Collection;
import java.util.Collections;

/**
 * Updates radiobutton and multi-checkbox searchers to be multi-select searchers
 *
 * @since v5.2
 */
public class UpgradeTask_Build810 implements UpgradeTask
{
    private static final String MULTI_SELECT_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher";
    private static final String RADIOBUTTONS_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:radiosearcher";
    private static final String MULTI_CHECKBOX_SEARCHER = "com.atlassian.jira.plugin.system.customfieldtypes:checkboxsearcher";
    private static final String FIELD_NAME = "customfieldsearcherkey";

    private final EntityEngine entityEngine;

    public UpgradeTask_Build810(EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "810";
    }

    @Override
    public String getShortDescription()
    {
        return "Updating radiobutton and multi-checkbox searchers to be multi-select searchers";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        entityEngine.execute(Update.into("CustomField").set(FIELD_NAME, MULTI_SELECT_SEARCHER).whereEqual(FIELD_NAME, RADIOBUTTONS_SEARCHER));
        entityEngine.execute(Update.into("CustomField").set(FIELD_NAME, MULTI_SELECT_SEARCHER).whereEqual(FIELD_NAME, MULTI_CHECKBOX_SEARCHER));
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
