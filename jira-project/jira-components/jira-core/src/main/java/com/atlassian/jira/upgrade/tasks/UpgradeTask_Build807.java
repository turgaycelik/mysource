package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.UpgradeTask;

import java.util.Collection;
import java.util.Collections;

/**
 * Remove any only draft workflows schemes that might be around after a downgrade from OnDemand 5.2
 * to BTF 5.1.x.
 *
 * @since v5.2
 */
public class UpgradeTask_Build807 implements UpgradeTask
{
    private final OfBizDelegator entityEngine;

    public UpgradeTask_Build807(OfBizDelegator entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "807";
    }

    @Override
    public String getShortDescription()
    {
        return "Removing old Draft Workflow Schemes";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        entityEngine.removeByAnd("DraftWorkflowSchemeEntity", Collections.<String, String>emptyMap());
        entityEngine.removeByAnd("DraftWorkflowScheme", Collections.<String, String>emptyMap());
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
