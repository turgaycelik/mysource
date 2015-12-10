package com.atlassian.jira.bc.project.property;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.EntityWithKeyPropertyHelper;
import com.atlassian.jira.event.project.property.ProjectPropertyDeletedEvent;
import com.atlassian.jira.event.project.property.ProjectPropertySetEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Function;

/**
 * Defines permission checking, events creation and persistence layer for {@link ProjectPropertyService}.
 *
 * @since v6.2
 */
public class ProjectPropertyHelper implements EntityWithKeyPropertyHelper<Project>
{
    private final DeletePropertyEventFunction deletePropertyEventFunction;
    private final I18nHelper i18n;
    private final ProjectByIdFunction projectByIdFunction;
    private final ProjectByKeyFunction projectByKeyFunction;
    private final HasEditPermissionFunction editPermissionFunction;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final HasViewPermissionFunction viewPermissionFunction;
    private final SetPropertyEventFunction setPropertyEventFunction;

    public ProjectPropertyHelper(I18nHelper i18n, ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.i18n = i18n;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.deletePropertyEventFunction = new DeletePropertyEventFunction();
        this.projectByIdFunction = new ProjectByIdFunction();
        this.projectByKeyFunction = new ProjectByKeyFunction();
        this.editPermissionFunction = new HasEditPermissionFunction();
        this.viewPermissionFunction = new HasViewPermissionFunction();
        this.setPropertyEventFunction = new SetPropertyEventFunction();
    }

    @Override
    public Function<String, Option<Project>> getEntityByKeyFunction()
    {
        return projectByKeyFunction;
    }

    @Override
    public CheckPermissionFunction<Project> hasEditPermissionFunction()
    {
        return editPermissionFunction;
    }

    @Override
    public CheckPermissionFunction<Project> hasReadPermissionFunction()
    {
        return viewPermissionFunction;
    }

    @Override
    public Function<Long, Option<Project>> getEntityByIdFunction()
    {
        return projectByIdFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, ProjectPropertySetEvent> createSetPropertyEventFunction()
    {
        return setPropertyEventFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, ProjectPropertyDeletedEvent> createDeletePropertyEventFunction()
    {
        return deletePropertyEventFunction;
    }

    @Override
    public EntityPropertyType getEntityPropertyType()
    {
        return EntityPropertyType.PROJECT_PROPERTY;
    }

    private class DeletePropertyEventFunction implements Function2<ApplicationUser, EntityProperty, ProjectPropertyDeletedEvent>
    {
        @Override
        public ProjectPropertyDeletedEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new ProjectPropertyDeletedEvent(entityProperty, user);
        }
    }

    private class ProjectByKeyFunction implements Function<String, Option<Project>>
    {
        @Override
        public Option<Project> apply(final String projectKey)
        {
            return Option.option(projectManager.getProjectObjByKey(projectKey));
        }
    }

    private class ProjectByIdFunction implements Function<Long, Option<Project>>
    {
        @Override
        public Option<Project> apply(final Long projectId)
        {
            return Option.option(projectManager.getProjectObj(projectId));
        }
    }

    private class HasEditPermissionFunction implements CheckPermissionFunction<Project>
    {
        @Override
        public ErrorCollection apply(final ApplicationUser user, final Project project)
        {
            return checkProjectPermission(user, project, ProjectAction.EDIT_PROJECT_CONFIG);
        }
    }

    private class HasViewPermissionFunction implements CheckPermissionFunction<Project>
    {
        @Override
        public ErrorCollection apply(final ApplicationUser user, final Project project)
        {
            return checkProjectPermission(user, project, ProjectAction.VIEW_PROJECT);
        }
    }

    private ErrorCollection checkProjectPermission(final ApplicationUser user, final Project project, final ProjectAction projectAction)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        if (!projectAction.hasPermission(permissionManager, user, project))
        {

            ErrorCollection.Reason reason = user == null ? ErrorCollection.Reason.NOT_LOGGED_IN : ErrorCollection.Reason.FORBIDDEN;
            errorCollection.addErrorMessage(i18n.getText(projectAction.getErrorKey()), reason);
        }
        return errorCollection;
    }

    private class SetPropertyEventFunction implements Function2<ApplicationUser, EntityProperty, ProjectPropertySetEvent>
    {
        @Override
        public ProjectPropertySetEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new ProjectPropertySetEvent(entityProperty, user);
        }
    }
}
