/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.index;

import java.util.Collection;

import com.atlassian.jira.index.ha.IndexRecoveryService;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class EditIndexRecoverySettings extends ProjectActionSupport
{
    private final IndexRecoveryService indexRecoveryService;

    // options
    private boolean recoveryEnabled;

    private IndexRecoveryUtil.Interval snapshotInterval;

    public EditIndexRecoverySettings(ProjectManager projectManager, PermissionManager permissionManager, final IndexRecoveryService indexRecoveryService)
    {
        super(projectManager, permissionManager);
        this.indexRecoveryService = indexRecoveryService;
    }

    public String doDefault() throws Exception
    {
        recoveryEnabled = indexRecoveryService.isRecoveryEnabled(getLoggedInApplicationUser());
        if (recoveryEnabled)
        {
            Long snapshotMillis = indexRecoveryService.getSnapshotInterval(getLoggedInApplicationUser());
            snapshotInterval = snapshotMillis == null ? IndexRecoveryUtil.DEFAULT_INTERVAL : IndexRecoveryUtil.intervalFromMillis(snapshotMillis);
        }
        else
        {
            recoveryEnabled = false;
            snapshotInterval = IndexRecoveryUtil.DEFAULT_INTERVAL;
        }
        return INPUT;
    }

    protected void doValidation()
    {
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        indexRecoveryService.updateRecoverySettings(getLoggedInApplicationUser(), recoveryEnabled, snapshotInterval.getMillis());
        return returnComplete("IndexAdmin.jspa");
    }

    public boolean isRecoveryEnabled()
    {
        return recoveryEnabled;
    }

    public void setRecoveryEnabled(final boolean recoveryEnabled)
    {
        this.recoveryEnabled = recoveryEnabled;
    }

    public String getSnapshotInterval()
    {
        return snapshotInterval.name();
    }

    public void setSnapshotInterval(final String snapshotInterval)
    {
        this.snapshotInterval = IndexRecoveryUtil.Interval.valueOf(snapshotInterval);
    }

    public Collection<TextOption> getIntervalOptions()
    {
        return IndexRecoveryUtil.getIntervalOptions(getI18nHelper());
    }


}
