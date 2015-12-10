package com.atlassian.jira.action.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;
import java.util.Set;

public class SelectComponentAssigneesUtilImpl implements SelectComponentAssigneesUtil
{
    private Map componentAssigneeTypes;
    private String fieldPrefix;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectComponentService projectComponentService;
    private PermissionManager permissionManager;

    public SelectComponentAssigneesUtilImpl(final JiraAuthenticationContext authenticationContext,
            final ProjectComponentService projectComponentService, final PermissionManager permissionManager)
    {
        this.authenticationContext = authenticationContext;
        this.projectComponentService = projectComponentService;
        this.permissionManager = permissionManager;
    }

    public ErrorCollection validate()
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        Set components = componentAssigneeTypes.keySet();
        GenericValue component;
        Long assigneeType;
        for (Object component1 : components)
        {
            component = (GenericValue) component1;
            assigneeType = (Long) componentAssigneeTypes.get(component);
            if (!isAssigneeTypeValid(component, assigneeType))
            {
                errorCollection.addError(fieldPrefix + component.getLong("id"),
                        authenticationContext.getI18nHelper().getText("admin.errors.invalid.default.assignee"));
            }
        }
        return errorCollection;
    }

    @Override
    public boolean hasPermission(Project project, User user)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);

    }

    public ErrorCollection execute(User user) throws GenericEntityException
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        @SuppressWarnings("unchecked")
        final Set<Map.Entry<GenericValue, Long>> componentEntries = componentAssigneeTypes.entrySet();
        for (final Map.Entry<GenericValue, Long> entry : componentEntries)
        {
            final Long assigneeType = entry.getValue();
            final Long componentId = entry.getKey().getLong("id");
            MutableProjectComponent projectComponent = MutableProjectComponent.copy(projectComponentService.find(user, errorCollection, componentId));
            projectComponent.setAssigneeType(assigneeType.longValue()); // although unboxing is automagic, leaving that for the sake of NPE worshippers
            projectComponentService.update(user, errorCollection, projectComponent);
        }

        return errorCollection;
    }

    private boolean isAssigneeTypeValid(GenericValue component, Long assigneeType)
    {
        return ComponentAssigneeTypes.isAssigneeTypeValid(component, assigneeType);
    }

    public Map getComponentAssigneeTypes()
    {
        return componentAssigneeTypes;
    }

    public void setComponentAssigneeTypes(Map componentAssigneeTypes)
    {
        this.componentAssigneeTypes = componentAssigneeTypes;
    }

    public String getFieldPrefix()
    {
        return fieldPrefix;
    }

    public void setFieldPrefix(String fieldPrefix)
    {
        this.fieldPrefix = fieldPrefix;
    }
}
