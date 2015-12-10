/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

public abstract class AbstractReindexUpgradeTask extends AbstractUpgradeTask
{
    protected AbstractReindexUpgradeTask()
    {
        super(true);
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
    }

    public String getShortDescription()
    {
        return "Signalling all data in JIRA should be reindexed.";
    }
}
