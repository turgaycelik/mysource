package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.Select;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.util.UpgradeEntityUtil;
import com.atlassian.jira.util.Visitor;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

/**
 * Grants Transition permission to all users with Browse permission.
 *
 * @since v6.3
 */
public class UpgradeTask_Build6321 extends AbstractUpgradeTask
{
    public static final String SCHEME_PERMISSIONS_TABLE = "SchemePermissions";
    public static final String PERMISSION_PARAMETER = "permission";
    public static final String ID_PARAMETER = "id";
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6321.class);

    public UpgradeTask_Build6321()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6321";
    }

    @Override
    public String getShortDescription()
    {
        return "Grants Transition permission to all users with Browse permission";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final long permissionToCopy;

        if (setupMode)
        {
            permissionToCopy = Permissions.EDIT_ISSUE;
        }
        else
        {
            permissionToCopy = Permissions.BROWSE;
        }
        final UpgradeEntityUtil upgradeEntityUtil = new UpgradeEntityUtil("UpgradeTask_Build6321", getEntityEngine());
        upgradeEntityUtil.deleteEntityByCondition(SCHEME_PERMISSIONS_TABLE,
                new EntityExpr(PERMISSION_PARAMETER, EntityOperator.EQUALS, (long) Permissions.TRANSITION_ISSUE));

        Select.from(SCHEME_PERMISSIONS_TABLE).whereEqual(PERMISSION_PARAMETER, permissionToCopy).
                runWith(getEntityEngine()).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue schemePermission)
            {
                final GenericValue transitionPermission = (GenericValue) schemePermission.clone();
                transitionPermission.set(PERMISSION_PARAMETER, (long) Permissions.TRANSITION_ISSUE);
                transitionPermission.set(ID_PARAMETER, null);
                try
                {
                    getOfBizDelegator().createValue(SCHEME_PERMISSIONS_TABLE, transitionPermission);
                }
                catch (DataAccessException e)
                {
                    upgradeEntityUtil.logEntity(SCHEME_PERMISSIONS_TABLE, schemePermission, "MIGRATE FAIL");
                    log.error("Problem while migrating to " + Permissions.getShortName((int) permissionToCopy) + " to TRANSITION_ISSUE permission", e);

                }
            }
        });

    }
}
