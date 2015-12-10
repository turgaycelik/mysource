package com.atlassian.jira.web.action.project.enterprise;

import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.action.component.SelectComponentAssigneesUtil;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.project.AbstractProjectAction;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SelectComponentAssignees extends AbstractProjectAction
{
    private static final String SECURITY_BREACH = "securitybreach";
    private static final String FIELD_PREFIX = "component_";

    private Long projectId;
    private final SelectComponentAssigneesUtil selectComponentAssigneesUtil;

    private final ProjectManager projectManager;

    public SelectComponentAssignees(final ProjectManager projectManager, final SelectComponentAssigneesUtil selectComponentAssigneesUtil)
    {
        this.projectManager = projectManager;
        this.selectComponentAssigneesUtil = selectComponentAssigneesUtil;
    }

    protected void doValidation()
    {
        Map components = getUpdateComponentAssigneeTypes();
        if (components != null)
        {
            selectComponentAssigneesUtil.setComponentAssigneeTypes(components);
            selectComponentAssigneesUtil.setFieldPrefix(getFieldPrefix());
            ErrorCollection errors = selectComponentAssigneesUtil.validate();
            if (errors != null && errors.hasAnyErrors())
            {
                addErrorCollection(errors);
            }
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!selectComponentAssigneesUtil.hasPermission(getProjectObj(), getLoggedInUser()))
        {
            return SECURITY_BREACH;
        }

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!selectComponentAssigneesUtil.hasPermission(getProjectObj(), getLoggedInUser()))
        {
            return SECURITY_BREACH;
        }

        selectComponentAssigneesUtil.setComponentAssigneeTypes(getUpdateComponentAssigneeTypes());
        addErrorCollection(selectComponentAssigneesUtil.execute(getLoggedInUser()));

        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/summary");
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Collection getComponents()
    {
        try
        {
            return projectManager.getComponents(getProject());
        }
        catch (GenericEntityException e)
        {
            return Collections.EMPTY_LIST;
        }
    }

    public String getComponentFieldName(GenericValue component)
    {
        return getFieldPrefix() + component.getLong("id");
    }

    public long getComponentAssigneeType(GenericValue component)
    {
        return ComponentUtils.getComponentAssigneeType(component);
    }

    public Map getComponentAssigneeTypes(GenericValue component)
    {
        return ComponentAssigneeTypes.getAssigneeTypes(component);
    }

    public static String getFieldPrefix()
    {
        return FIELD_PREFIX;
    }

    public GenericValue getProject() throws GenericEntityException
    {
        return projectManager.getProject(getProjectId());
    }

    public Project getProjectObj()
    {
        return projectManager.getProjectObj(getProjectId());
    }

    private Map getUpdateComponentAssigneeTypes()
    {
        Map returnedMap = new HashMap();

        Map parameters = ActionContext.getParameters();
        Set keys = parameters.keySet();
        for (final Object key1 : keys)
        {
            String key = (String) key1;
            if (key.startsWith(getFieldPrefix()))
            {
                Long componentId = new Long(key.substring(getFieldPrefix().length()));

                GenericValue component = projectManager.getComponent(componentId);
                returnedMap.put(component, ParameterUtils.getLongParam(parameters, key));
            }
        }
        return returnedMap;
    }

    public String getAvatarUrl()
    {
        return ActionContext.getRequest().getContextPath() + "/secure/projectavatar?pid=" + getProjectId() + "&avatarId=" + getAvatarId();
    }
}
