package com.atlassian.jira.upgrade.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;

/**
 * Upgrade task that will move all GlobalPermission entries from the SchemePermissions table to the brand new
 * GlobalPermissionEntry table.
 */
public class UpgradeTask_Build6304 extends AbstractUpgradeTask
{

    private final OfBizDelegator ofBizDelegator;

    private final static String SCHEME_PERMISSIONS_TABLE = "SchemePermissions";
    private final static String SCHEME_PERMISSIONS_SCHEME_COLUMN = "scheme";
    private final static String SCHEME_PERMISSIONS_PERMISSION_COLUMN = "permission";
    private final static String SCHEME_PERMISSIONS_GROUP_COLUMN = "parameter";
    private final static String SCHEME_PERMISSIONS_TYPE_COLUMN = "type";

    private final static String TYPE_EXPECTED_VALUE = "group";

    private final static String GLOBAL_PERMISSION_ENTRY_TABLE = "GlobalPermissionEntry";
    private final static String GLOBAL_PERMISSION_PERMISSION_COLUMN = "permission";
    private final static String GLOBAL_PERMISSION_GROUP_COLUMN = "group_id";

    private static Map<Long, String> GLOBAL_PERMISSION_ID_TRANSLATION;

    static
    {
        Map<Long, String> map = Maps.newHashMap();
        map.put(0l, "ADMINISTER");
        map.put(1l, "USE");
        map.put(44l, "SYSTEM_ADMIN");
        map.put(22l, "CREATE_SHARED_OBJECTS");
        map.put(24l, "MANAGE_GROUP_FILTER_SUBSCRIPTIONS");
        map.put(33l, "BULK_CHANGE");
        map.put(27l, "USER_PICKER");
        GLOBAL_PERMISSION_ID_TRANSLATION = Collections.unmodifiableMap(map);
    }

    public UpgradeTask_Build6304(OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6304";
    }

    @Override
    public String getShortDescription()
    {
        return "Migrates global permission entries out of SchemePermissions and into it's own table";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        // Get all rows where the 'scheme' column is null. This means that it is a global permission
        final List<GenericValue> oldGlobalPermissions = ofBizDelegator.findByCondition(SCHEME_PERMISSIONS_TABLE, new EntityExpr(SCHEME_PERMISSIONS_SCHEME_COLUMN, EQUALS, null), null);
        for (GenericValue gv : oldGlobalPermissions)
        {
            Long id = gv.getLong(SCHEME_PERMISSIONS_PERMISSION_COLUMN);
            String group = gv.getString(SCHEME_PERMISSIONS_GROUP_COLUMN);
            String type = gv.getString(SCHEME_PERMISSIONS_TYPE_COLUMN);

            // Check invariants
            if (TYPE_EXPECTED_VALUE.equals(type) && GLOBAL_PERMISSION_ID_TRANSLATION.containsKey(id))
            {
                String permissionKey = GLOBAL_PERMISSION_ID_TRANSLATION.get(id);
                // Null group = anon allowed.
                migrateValue(permissionKey, StringUtils.defaultIfBlank(group, null));
            }
        }
        ComponentAccessor.getGlobalPermissionManager().clearCache();
    }

    private void migrateValue(String permissionKey, String group)
    {
        Map<String, String> conditions = Maps.newHashMap();
        conditions.put(GLOBAL_PERMISSION_PERMISSION_COLUMN, permissionKey);
        conditions.put(GLOBAL_PERMISSION_GROUP_COLUMN, group);
        final List<GenericValue> existingPermissions = ofBizDelegator.findByAnd(GLOBAL_PERMISSION_ENTRY_TABLE, conditions);
        if (existingPermissions.isEmpty())
        {
            ofBizDelegator.createValue(GLOBAL_PERMISSION_ENTRY_TABLE,
                    FieldMap.build(GLOBAL_PERMISSION_PERMISSION_COLUMN, permissionKey)
                            .add(GLOBAL_PERMISSION_GROUP_COLUMN, group));
        }
    }
}
