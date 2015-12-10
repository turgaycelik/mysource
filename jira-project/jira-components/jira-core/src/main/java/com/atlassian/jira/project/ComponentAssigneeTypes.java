package com.atlassian.jira.project;

import java.util.Map;

import com.atlassian.jira.action.component.ComponentUtils;

import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

public class ComponentAssigneeTypes extends AssigneeTypes
{
    private static final String NO_DEFAULT_MESSAGE = "Please select a Default Assignee";

    public static boolean isProjectDefault(Long defaultAssigneeType)
    {
        return defaultAssigneeType == null || defaultAssigneeType == PROJECT_DEFAULT;
    }

    public static boolean isProjectLead(Long defaultAssigneeType)
    {
        return isAssigneeType(defaultAssigneeType, PROJECT_LEAD);
    }

    public static boolean isUnassigned(Long defaultAssigneeType)
    {
        return isAssigneeType(defaultAssigneeType, UNASSIGNED);
    }

    public static boolean isComponentLead(Long defaultAssigneeType)
    {
        return isAssigneeType(defaultAssigneeType, COMPONENT_LEAD);
    }

    public static Map getAssigneeTypes(GenericValue component)
    {

        Map<Long,String> assigneesTypesMap = Maps.newLinkedHashMap();
        assigneesTypesMap.put(PROJECT_DEFAULT, PRETTY_PROJECT_DEFAULT);

        // Check if unassigned is allowed
        if (isAllowUnassigned())
        {
            assigneesTypesMap.put(UNASSIGNED, PRETTY_UNASSIGNED);
        }
        else
        {
            assigneesTypesMap.put(-UNASSIGNED, PRETTY_NOT_ALLOWED_UNASSIGNED);
        }

        // Check if project lead is assignable
        if (ComponentUtils.isProjectLeadAssignable(component))
        {
            assigneesTypesMap.put(PROJECT_LEAD, PRETTY_PROJECT_LEAD);
        }
        else
        {
            assigneesTypesMap.put(-PROJECT_LEAD, PRETTY_NOT_ASSIGNABLE_PROJECT_LEAD);
        }

        // Check whether component lead exists and whether he/she is assignable
        if (component.getString("lead") == null)
        {
            assigneesTypesMap.put(-COMPONENT_LEAD, PRETTY_COMPONENT_LEAD_DOES_NOT_EXIST);
        }
        else
        {
            if (ComponentUtils.isComponentLeadAssignable(component))
            {
                assigneesTypesMap.put(COMPONENT_LEAD, PRETTY_COMPONENT_LEAD);
            }
            else
            {
                assigneesTypesMap.put(-COMPONENT_LEAD, PRETTY_NOT_ASSIGNABLE_COMPONENT_LEAD);
            }
        }
        return assigneesTypesMap;
    }

    private static boolean isAssigneeType(Long defaultAssigneeType, long assigneeType)
    {
        return defaultAssigneeType != null && defaultAssigneeType == assigneeType;
    }

    public static boolean isAssigneeTypeValid(GenericValue component, Long assigneeType)
    {
        return assigneeType == null || ComponentUtils.getAssigneeType(component, assigneeType) == assigneeType;
    }

    public static String getPrettyAssigneeType(Long defaultAssigneeType)
    {
        if (isUnassigned(defaultAssigneeType))
        {
            return PRETTY_UNASSIGNED;
        }
        else if (isComponentLead(defaultAssigneeType))
        {
            return PRETTY_COMPONENT_LEAD;
        }
        else if (isProjectDefault(defaultAssigneeType))
        {
            return PRETTY_PROJECT_DEFAULT;
        }
        else if (isProjectLead(defaultAssigneeType))
        {
            return PRETTY_PROJECT_LEAD;
        }
        else
        {
            return NO_DEFAULT_MESSAGE;
        }
    }
}
