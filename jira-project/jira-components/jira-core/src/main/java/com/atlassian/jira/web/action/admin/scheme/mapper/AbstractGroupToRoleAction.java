package com.atlassian.jira.web.action.admin.scheme.mapper;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.mapper.SchemeTransformResults;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.util.GroupToNotificationSchemeMapper;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemeToolAction;
import webwork.action.ActionContext;

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 */
public class AbstractGroupToRoleAction extends AbstractSchemeToolAction
{
    public static final String TRANSFORM_RESULTS_KEY = "__schemeGroupsToRoleTransformResults";
    public static final String GROUP_TO_ROLE_MAP_SESSION_KEY = "__schemeGroupsToRoleMapping_sessionkey";
    public static final String SCHEME_TOOL_NAME = "SchemeGroupToRoleTool";

    public AbstractGroupToRoleAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
    }

    public Set getUniqueGroupsForSelectedSchemes()
    {
        Set<String> uniqueGroupNames = new TreeSet<String>();

        // run through all schemes and grab all the groups
        for (final Scheme scheme : getSchemeObjs())
        {
            // Iterate over all the entities of this scheme looking for those of type 'group'
            for (SchemeEntity schemeEntity : scheme.getEntities())
            {
                // Only ever transform schemeEntities that are of type 'group' or 'Group_Dropdown'
                if (GroupDropdown.DESC.equals(schemeEntity.getType()) || GroupToNotificationSchemeMapper.GROUP_DROPDOWN.equals(schemeEntity.getType()))
                {
                    if (schemeEntity.getParameter() != null)
                    {
                        uniqueGroupNames.add(schemeEntity.getParameter());
                    }
                }
            }
        }
        return uniqueGroupNames;
    }

    public SchemeTransformResults getSchemeTransformResults()
    {
        return (SchemeTransformResults) ActionContext.getSession().get(TRANSFORM_RESULTS_KEY);
    }

    public String getToolName()
    {
        return SCHEME_TOOL_NAME;
    }
}
