package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * JDEV-26606 Modify permissions schemes to allow project role 'Users' to link issues if they can comment,
 * only if both permissions are still like in the default permission scheme.
 *
 * @since v6.2
 */
public class UpgradeTask_Build6211 extends AbstractUpgradeTask
{
    public static final String USERS_PROJECT_ROLE_ID = "10000";
    public static final String DEVELOPERS_PROJECT_ROLE_ID = "10001";

    private final OfBizDelegator ofBizDelegator;
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6211.class);

    public UpgradeTask_Build6211(OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6211";
    }

    @Override
    public String getShortDescription()
    {
        return "JDEV-26606 Modify permissions schemes to allow project role 'Users' to link issues if they can comment,"
                + " only if both permissions are still like in the default permission scheme.";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
       List<GenericValue> permissionSchemesGVs = ofBizDelegator.findAll("PermissionScheme");

        for(GenericValue scheme : permissionSchemesGVs)
        {
            try
            {
                Long schemeId = scheme.getLong("id");
                List<GenericValue> commentPermissionSchemeGVs = ofBizDelegator.findByAnd("SchemePermissions", ImmutableMap.of("scheme", schemeId, "permission", Permissions.COMMENT_ISSUE, "type", "projectrole"));

                // We only add Link permission to Users if they also have comment permission
                // We don't want to mess with permissions if any other project roles have been added to Comment ... this indicates a customised permission scheme
                if (commentPermissionSchemeGVs.size() == 1 && commentPermissionSchemeGVs.get(0).getString("parameter").equals(USERS_PROJECT_ROLE_ID))
                {
                    List<GenericValue> linkPermissionSchemeGVs = ofBizDelegator.findByAnd("SchemePermissions", ImmutableMap.of("scheme", schemeId, "permission", Permissions.LINK_ISSUE, "type", "projectrole"));

                    // Same here: we don't want to mess with permissions if any other project roles than the default one have been added to Link
                    // as this indicates a customised permission scheme
                    if (linkPermissionSchemeGVs.size() == 1 && linkPermissionSchemeGVs.get(0).getString("parameter").equals(DEVELOPERS_PROJECT_ROLE_ID))
                    {
                        log.info("Updating role to 'Users' in scheme '" + scheme.getString("name") + "' for link issue permission");
                        GenericValue permissionSchemeGV = linkPermissionSchemeGVs.get(0);
                        permissionSchemeGV.setString("parameter", USERS_PROJECT_ROLE_ID);
                        ofBizDelegator.store(permissionSchemeGV);
                    }
                }
            }
            catch (RuntimeException e)
            {
                //Logging and swallowing the exception as we treat this update as "optional"
                //Unexpected errors probably mean that the conditons to update the permission are not met
                log.warn("Unable to update scheme '" + scheme.getString("name") + "' to allow project role 'Users' to link issues", e);
            }
        }
    }
}
