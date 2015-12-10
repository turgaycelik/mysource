package com.atlassian.jira.security;

import com.atlassian.jira.scheme.AbstractSchemeTypeManager;
import com.atlassian.jira.security.type.SecurityType;

import java.util.Map;

/**
 * This class reads the permission-types.xml file for the different types of issue securities that are used.
 * These can be group, CurrentReporter , Project Lead etc
 *
 * These types are used for general permission types as well as Issue Security types.
 */
public abstract class AbstractSecurityTypeManager extends AbstractSchemeTypeManager<SecurityType> implements SecurityTypeManager
{
    private Map<String, SecurityType> schemeTypes;

    public SecurityType getSecurityType(String id)
    {
        return getSchemeType(id);
    }

    public Map<String, SecurityType> getSecurityTypes()
    {
        return getSchemeTypes();
    }

    public void setSecurityTypes(Map<String, SecurityType> securityTypes)
    {
        setSchemeTypes(securityTypes);
    }

    public String getResourceName()
    {
        return "permission-types.xml";
    }

    public Map<String, SecurityType> getSchemeTypes()
    {
        return schemeTypes;
    }

    public void setSchemeTypes(Map<String, SecurityType> schemeType)
    {
        schemeTypes = schemeType;
    }

    public boolean hasSecurityType(String securityTypeStr)
    {
        getTypes();
        return schemeTypes.containsKey(securityTypeStr);
    }
}
