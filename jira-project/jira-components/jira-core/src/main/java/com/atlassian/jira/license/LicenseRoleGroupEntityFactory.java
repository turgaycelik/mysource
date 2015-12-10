package com.atlassian.jira.license;

import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;


public class LicenseRoleGroupEntityFactory extends AbstractEntityFactory<LicenseRoleGroupEntry>
{
    static final String LICENSE_ROLE_NAME = "licenseRoleName";
    static final String GROUP_ID = "groupId";

    @Override
    public Map<String, Object> fieldMapFrom(final LicenseRoleGroupEntry value)
    {
        return new FieldMap()
                .add(LICENSE_ROLE_NAME, value.getLicenseRoleName())
                .add(GROUP_ID, value.getGroupId());
    }

    @Override
    public String getEntityName()
    {
        return "LicenseRoleGroup";
    }

    @Override
    public LicenseRoleGroupEntry build(final GenericValue genericValue)
    {
        return new LicenseRoleGroupEntry(genericValue.getString(LICENSE_ROLE_NAME), genericValue.getString(GROUP_ID));
    }
}
