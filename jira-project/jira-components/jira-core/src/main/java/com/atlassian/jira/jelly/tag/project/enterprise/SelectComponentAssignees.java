package com.atlassian.jira.jelly.tag.project.enterprise;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.component.SelectComponentAssigneesUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.JellyUtils;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class SelectComponentAssignees extends JiraDynaBeanTagSupport
{
    private static final transient Logger log = Logger.getLogger(SelectComponentAssignees.class);
    private static final String PROJECT_KEY = "project-key";
    private static final String COMPONENT_NAME = "componentName";
    private static final String ASSIGNEE_TYPE = "assigneeType";
    private static final String PROJECT_DEFAULT = "projectDefault";
    private static final String COMPONENT_LEAD = "componentLead";
    private static final String PROJECT_LEAD = "projectLead";
    private static final String UNASSIGNED = "unassigned";

    private final SelectComponentAssigneesUtil selectComponentAssigneesUtil;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectManager projectManager;

    public SelectComponentAssignees(final JiraAuthenticationContext authenticationContext, final ProjectManager projectManager)
    {
        this.authenticationContext = authenticationContext;
        this.projectManager = projectManager;
        this.selectComponentAssigneesUtil = ComponentAccessor.getComponentOfType(SelectComponentAssigneesUtil.class);
    }

    public void doTag(XMLOutput output) throws JellyTagException
    {
        validateAttributes();

        try
        {
            final Map updateComponentAssigneeTypes = getUpdateComponentAssigneeTypes();
            selectComponentAssigneesUtil.setComponentAssigneeTypes(updateComponentAssigneeTypes);
            ErrorCollection errors = selectComponentAssigneesUtil.execute(authenticationContext.getLoggedInUser());
            if (errors != null && errors.hasAnyErrors())
            {
                JellyUtils.processErrorCollection(errors);
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error while running tag", e);
        }
    }

    private void validateAttributes() throws JellyTagException
    {
        if (!paramSpecified(PROJECT_KEY) || StringUtils.isEmpty(getProjectKey()))
        {
            throw new MissingAttributeException(PROJECT_KEY);
        }

        if (!paramSpecified(COMPONENT_NAME) || StringUtils.isEmpty(getComponentName()))
        {
            throw new MissingAttributeException(COMPONENT_NAME);
        }

        if (!paramSpecified(ASSIGNEE_TYPE) || getAssigneeType() == null)
        {
            throw new MissingAttributeException(ASSIGNEE_TYPE);
        }

        Project project = projectManager.getProjectObjByKey(getProjectKey());
        if (selectComponentAssigneesUtil.hasPermission(project, authenticationContext.getLoggedInUser()))
        {
            selectComponentAssigneesUtil.setComponentAssigneeTypes(getUpdateComponentAssigneeTypes());
            selectComponentAssigneesUtil.setFieldPrefix("component_");
            ErrorCollection errors = selectComponentAssigneesUtil.validate();
            if (errors != null && errors.hasAnyErrors())
            {
                JellyUtils.processErrorCollection(errors);
            }
        }
        else
        {
            throw new JellyTagException("User " + authenticationContext.getLoggedInUser() + " does not have " +
                    Permissions.getShortName(Permissions.ADMINISTER) + " permission to select component assignee.");
        }
    }

    private boolean paramSpecified(String paramName)
    {
        return getProperties().containsKey(paramName);
    }

    private String getProjectKey()
    {
        return (String) getProperties().get(PROJECT_KEY);
    }

    private String getComponentName()
    {
        return (String) getProperties().get(COMPONENT_NAME);
    }

    private Long getAssigneeType() throws JellyTagException
    {
        String assigneeType = (String) getProperties().get(ASSIGNEE_TYPE);
        if (PROJECT_DEFAULT.equals(assigneeType))
        {
            return ComponentAssigneeTypes.PROJECT_DEFAULT;
        }
        else if (COMPONENT_LEAD.equals(assigneeType))
        {
            return ComponentAssigneeTypes.COMPONENT_LEAD;
        }
        else if (PROJECT_LEAD.equals(assigneeType))
        {
            return ComponentAssigneeTypes.PROJECT_LEAD;
        }
        else if (UNASSIGNED.equals(assigneeType))
        {
            return ComponentAssigneeTypes.UNASSIGNED;
        }
        else
        {
            throw new JellyTagException("Invalid assigneetype value: " + assigneeType);
        }
    }

    private Map getUpdateComponentAssigneeTypes() throws JellyTagException
    {
        Map componentAssigneeTypes = new HashMap();
        GenericValue project = projectManager.getProjectByKey(getProjectKey());
        for (GenericValue componentGv : projectManager.getComponents(project))
        {
            if (componentGv.get("name").equals(getComponentName()))
            {
                componentAssigneeTypes.put(componentGv, getAssigneeType());
            }
            else
            {
                componentAssigneeTypes.put(componentGv, componentGv.get("assigneetype"));
            }
        }
        return componentAssigneeTypes;
    }
}
