package com.atlassian.jira.security.plugin;

import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.GlobalPermissionEntry;

import org.ofbiz.core.entity.GenericValue;

public class GlobalPermissionEntityFactory extends AbstractEntityFactory<GlobalPermissionEntry>
{
    public static final String PERMISSION = "permission";
    public static final String GROUP = "group_id";

    @Override
    public String getEntityName()
    {
        return Entity.Name.GLOBAL_PERMISSION_ENTRY;
    }

    @Override
    public Map<String, Object> fieldMapFrom(final GlobalPermissionEntry value)
    {
        return new FieldMap()
                .add(GROUP, value.getGroup())
                .add(PERMISSION, value.getPermissionKey());
    }

    @Override
    public GlobalPermissionEntry build(final GenericValue gV)
    {
        return new GlobalPermissionEntry(gV.getString(PERMISSION), gV.getString(GROUP));
    }
}
