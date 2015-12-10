package com.atlassian.jira.bc.project.component;

import java.util.Collection;
import java.util.Iterator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.event.bc.project.component.ProjectComponentMergedEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class DefaultProjectComponentService implements ProjectComponentService
{

    private static final Logger LOG = Logger.getLogger(DefaultProjectComponentService.class);

    public static final String KEY_USER_NO_PERMISSION = "admin.projects.component.usernopermission";
    public static final String KEY_USER_NO_PERMISSION_WITH_USER = "admin.projects.component.usernopermission.withuser";
    public static final String KEY_NO_SUCH_COMPONENT = "admin.projects.component.nosuchcomponent";
    public static final String KEY_NO_SUCH_COMPONENT_WITH_ID = "admin.projects.component.nosuchcomponent.withid";
    public static final String KEY_NO_SUCH_COMPONENT_TO_SWAP_WITH_ID = "admin.projects.component.nosuchcomponent.toswap.withid";

    public static final String KEY_ID_NULL = "admin.projects.component.nullId";
    public static final String KEY_PROJECT_ID_NULL = "admin.projects.component.nullprojectid";
    public static final String KEY_PROJECT_ID_NOT_FOUND = "admin.projects.component.nosuchprojectid";
    public static final String KEY_NAME_NOT_SET = "admin.projects.component.namenotset";
    public static final String KEY_NAME_NOT_UNIQUE = "admin.projects.component.namenotunique";
    public static final String KEY_USER_DOES_NOT_EXIST = "admin.projects.component.userdoesnotexist";
    public static final String KEY_ASSIGNEE_TYPE_INVALID = "admin.projects.component.assignee.type.invalid";


    /**
     * project ID field name
     */
    public static final String FIELD_PROJECT_ID = "projectId";

    /**
     * name field name
     */
    public static final String FIELD_NAME = "name";

    /**
     * lead field name
     */
    public static final String FIELD_COMPONENT_LEAD = "componentLead";

    /**
     * assignee type field name
     */
    public static final String FIELD_ASSIGNEE_TYPE = "assigneeType";

    private final ProjectComponentManager projectComponentManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private final EventPublisher eventPublisher;

    public DefaultProjectComponentService(JiraAuthenticationContext jiraAuthenticationContext, PermissionManager permissionManager,
            ProjectComponentManager projectComponentManager, ProjectManager projectManager,
            IssueManager issueManager, UserManager userManager, final EventPublisher eventPublisher)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.projectComponentManager = projectComponentManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new ProjectComponent object associated with the project with the ID specified and with the values given.
     * <p/>
     * Validates the values - stores and returns the ProjectComponent if validation successful. Otherwise, null is
     * returned.
     *
     * @param user            user performing this operation
     * @param errorCollection collection to add error messages to if validation and permission checks fail - String objects
     * @param name            name of component
     * @param description     description of component
     * @param lead            user name associated with component
     * @param projectId       ID of project that component is associated with
     * @return new instance of ProjectComponent with the values specified
     */
    @Override
    public ProjectComponent create(final User user, final ErrorCollection errorCollection, final String name, final String description, final String lead, final Long projectId)
    {

        return new Handler<ProjectComponent>(errorCollection, this)
        {
            @Override
            void checkPermissions()
            {
                checkProjectAdminPermission(this, projectId, user);
            }

            @Override
            void validateData()
            {
                validateProjectId(this, projectId);
                validateName(this, null, name, projectId);
                validateLead(this, lead);
            }

            @Override
            void executeOnSuccess()
            {
                try
                {
                    result = projectComponentManager.create(name, description, lead, AssigneeTypes.PROJECT_DEFAULT, projectId);
                }
                catch (IllegalArgumentException e)
                {
                    LOG.error("Failed to create project component", e);
                }
            }

        }.run();
    }

    @Override
    public ProjectComponent create(final User user, final ErrorCollection errorCollection, final String name, final String description, final String lead, final Long projectId, final Long assigneeType)
    {
        return new Handler<ProjectComponent>(errorCollection, this)
        {
            @Override
            void checkPermissions()
            {
                checkProjectAdminPermission(this, projectId, user);
            }

            @Override
            void validateData()
            {
                validateProjectId(this, projectId);
                validateName(this, null, name, projectId);
                validateLead(this, lead);
                validateAssigneeType(this, assigneeType);
            }

            @Override
            void executeOnSuccess()
            {
                try
                {
                    Long assigneeTypeParam = assigneeType == null ? AssigneeTypes.PROJECT_DEFAULT : assigneeType;

                    result = projectComponentManager.create(name, description, lead, assigneeTypeParam, projectId);
                }
                catch (IllegalArgumentException e)
                {
                    LOG.error("Failed to create project component", e);
                }
            }

        }.run();
    }

    /**
     *
     * @param user            user performing this operation or <code>null</code> for anonymous access
     * @param errorCollection collection to add error messages to if validation and permission checks fail - String objects
     * @param id component id
     * @return project component or null and then <code>errorCollection</code> should contain some error information
     */
    @Override
    public ProjectComponent find(final User user, final ErrorCollection errorCollection, final Long id)
    {
        return new Handler<ProjectComponent>(errorCollection, this)
        {
            @Override
            void checkPermissions()
            {
                if (id == null)
                {
                    addErrorKey(KEY_ID_NULL, null, ErrorCollection.Reason.FORBIDDEN);
                    return;
                }

                final Long projectId;
                try
                {
                    projectId = projectComponentManager.findProjectIdForComponent(id);
                }
                catch (EntityNotFoundException e)
                {
                    addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, id.toString(), ErrorCollection.Reason.FORBIDDEN);
                    return;
                }
                checkCanAccessComponentPermission(this, projectId, user);

            }

            @Override
            void validateData()
            {
                if (id == null)
                {
                    addErrorKey(KEY_ID_NULL, null, ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }

            @Override
            void executeOnSuccess()
            {
                try
                {
                    result = projectComponentManager.find(id);
                }
                catch (EntityNotFoundException e)
                {
                    if (id != null)
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, id.toString(), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                    else
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.SERVER_ERROR);
                    }
                }
            }

        }.run();
    }

    @Override
    public Collection<ProjectComponent> findAllForProject(final ErrorCollection errorCollection, final Long projectId)
    {
        return new Handler<Collection<ProjectComponent>>(errorCollection, this)
        {
            @Override
            void validateData()
            {
                validateProjectId(this, projectId);
            }

            @Override
            void executeOnSuccess()
            {
                result = projectComponentManager.findAllForProject(projectId);
            }

        }.run();
    }

    @Override
    public ProjectComponent update(final User user, final ErrorCollection errorCollection, final MutableProjectComponent component)
    {
        return new Handler<ProjectComponent>(errorCollection, this)
        {
            @Override
            void checkPermissions()
            {
                checkProjectAdminPermission(this, component, user);
            }

            @Override
            void validateData()
            {

                // validate lead
                validateLead(this, component.getLead());

                // validate name
                Long id = component.getId();
                try
                {
                    ProjectComponent oldComponent = projectComponentManager.find(id);
                    String oldName = oldComponent.getName();

                    if (!oldName.equalsIgnoreCase(component.getName()))
                    {
                        validateName(this, oldName, component.getName(), oldComponent.getProjectId());
                    }
                }
                catch (EntityNotFoundException e)
                {
                    if (id != null)
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, id.toString(), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                    else
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
            }

            @Override
            void executeOnSuccess()
            {
                try
                {

                    result = projectComponentManager.update(component);
                }
                catch (EntityNotFoundException e)
                {
                    Long id = component.getId();
                    if (id != null)
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, id.toString(), ErrorCollection.Reason.SERVER_ERROR);
                    }
                    else
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.SERVER_ERROR);
                    }
                }
            }

        }.run();
    }

    private void delete(final JiraServiceContext context, final Long componentId)
    {
        new Handler<Void>(context.getErrorCollection(), this)
        {
            @Override
            void checkPermissions()
            {
                try
                {
                    ProjectComponent component = projectComponentManager.find(componentId);
                    checkProjectAdminPermission(this, component.getProjectId(), context.getLoggedInUser());
                }
                catch (EntityNotFoundException e)
                {
                    if (componentId != null)
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, componentId.toString(), ErrorCollection.Reason.FORBIDDEN);
                    }
                    else
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.FORBIDDEN);
                    }
                }

            }

            @Override
            void executeOnSuccess()
            {
                try
                {
                    projectComponentManager.delete(componentId);
                }
                catch (EntityNotFoundException e)
                {
                    if (componentId != null)
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, componentId.toString(), ErrorCollection.Reason.SERVER_ERROR);
                    }
                    else
                    {
                        addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.SERVER_ERROR);
                    }
                }
            }

        }.run();
    }

    @Override
    public void deleteComponentForIssues(final JiraServiceContext context, Long componentId)
    {
        final ErrorCollection errorCollection = context.getErrorCollection();
        final User user = context.getLoggedInUser();

        // Validate that we can find the component we are deleting
        ProjectComponent component = validateAndGetComponent(componentId, KEY_NO_SUCH_COMPONENT, KEY_NO_SUCH_COMPONENT_WITH_ID,  errorCollection);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Validate that we have permission to delete and swap the components
        checkProjectAdminPermission(errorCollection, component, user);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Now lets remove the component from all affected issues.
        removeComponentForAffectedIssues(context, component);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Now do the actual delete if there were no errors swaping components
        delete(context, componentId);
    }

    @Override
    public void deleteAndSwapComponentForIssues(final JiraServiceContext context, Long componentId, Long swapComponentId)
    {
        // Validate that we can find the component we want to swap with
        final ErrorCollection errorCollection = context.getErrorCollection();
        final User user = context.getLoggedInUser();
        ProjectComponent swapComponent = validateAndGetComponent(swapComponentId, KEY_NO_SUCH_COMPONENT_TO_SWAP_WITH_ID, KEY_NO_SUCH_COMPONENT_TO_SWAP_WITH_ID,  errorCollection);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Validate that we can find the component we are deleting
        ProjectComponent component = validateAndGetComponent(componentId, KEY_NO_SUCH_COMPONENT, KEY_NO_SUCH_COMPONENT_WITH_ID,  errorCollection);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Validate that we have permission to delete and swap the components
        checkProjectAdminPermission(errorCollection, component, user);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Before we delete the component we want to swap the component to be deleted with a specified component
        // generating change history for each of the changes
        swapComponentForAffectedIssues(context, component, swapComponent);
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Now do the actual delete if there were no errors swaping components
        delete(context, componentId);
    }

    private void removeComponentForAffectedIssues(final JiraServiceContext context, final ProjectComponent component)
    {
        //call the swapComponent method with a null swapcomponent.  Effectively means the old compoment will be removed
        //and nothing gets swapped in.
        swapComponentForAffectedIssues(context, component, null);
    }

    private void swapComponentForAffectedIssues(final JiraServiceContext context, final ProjectComponent component, final ProjectComponent swapComponent)
    {
        // Get all issues that reference the component we are going to delete
        Collection<Long> affectedIssues = projectComponentManager.getIssueIdsWithComponent(component);

        for (final Long issueId : affectedIssues)
        {
            MutableIssue issue = issueManager.getIssueObject(issueId);
            Collection<GenericValue> newComponents = getNewComponents(issue, component, swapComponent);
            issue.setComponents(newComponents);

            // Use the backend issue update action to update the issue
            issueManager.updateIssue(context.getLoggedInUser(), issue, EventDispatchOption.ISSUE_UPDATED, false);
        }

        // publish project component merged event if necessary
        if (!affectedIssues.isEmpty() && swapComponent != null)
        {
            eventPublisher.publish(new ProjectComponentMergedEvent(swapComponent, component));
        }
    }

    private ProjectComponent validateAndGetComponent(Long componentId, String errorMsgNoId, String errorMsgNoCompWithId, ErrorCollection errorCollection)
    {
        if(componentId == null)
        {
            errorCollection.addErrorMessage(translateKeyToMessage(errorMsgNoId, null), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }

        try
        {
            return projectComponentManager.find(componentId);
        }
        catch (EntityNotFoundException e)
        {
            errorCollection.addErrorMessage(translateKeyToMessage(errorMsgNoCompWithId, componentId.toString()), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }

    /**
     * Gets a list of new components.
     * @param issue The issue for which we are changeing components
     * @param oldComponent The component being removed.
     * @param swapComponent The component being swapped in.  May be null (in which case nothing gets swapped in.
     * @return A list of compoments to save back to the issue.
     */
    private Collection<GenericValue> getNewComponents(Issue issue, ProjectComponent oldComponent, ProjectComponent swapComponent)
    {
        Collection<GenericValue> newComponents = issue.getComponents();

        for (Iterator<GenericValue> iterator1 = newComponents.iterator(); iterator1.hasNext();)
        {
            GenericValue componentGV = iterator1.next();
            if(componentGV.getLong("id").equals(oldComponent.getId()))
            {
                iterator1.remove();
            }
        }
        if(swapComponent != null)
        {
            newComponents.add(swapComponent.getGenericValue());
        }

        return newComponents;
    }

    /**
     * **************************************
     * ****   Permision Check Methods   *****
     * **************************************
     */

    // This is crap and should not take a handler thats why we have duplication below
    private void checkProjectAdminPermission(Handler handler, Long projectId, User user)
    {
        boolean hasProjectAdminPermission = permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, projectManager.getProjectObj(projectId), user);
        if (!hasProjectAdminPermission)
        {
            if (user != null)
            {
                handler.addErrorKey(KEY_USER_NO_PERMISSION_WITH_USER, user.getName(), ErrorCollection.Reason.FORBIDDEN);
            }
            else
            {
                handler.addErrorKey(KEY_USER_NO_PERMISSION, null, ErrorCollection.Reason.NOT_LOGGED_IN);
            }
        }
    }

    // This is crap and should not take a handler thats why we have duplication below
    private void checkProjectAdminPermission(Handler handler, ProjectComponent component, User user)
    {
        if (component == null)
        {
            handler.addErrorKey(KEY_NO_SUCH_COMPONENT, null, ErrorCollection.Reason.FORBIDDEN);
        }
        else
        {
            Long projectId = component.getProjectId();
            checkProjectAdminPermission(handler, projectId, user);
        }
    }

    private void checkProjectAdminPermission(ErrorCollection errorCollection, ProjectComponent component, User user)
    {
        if (component == null)
        {
            errorCollection.addErrorMessage(translateKeyToMessage(KEY_NO_SUCH_COMPONENT, null), ErrorCollection.Reason.FORBIDDEN);
        }
        else
        {
            Long projectId = component.getProjectId();
            checkProjectAdminPermission(errorCollection, projectId, user);
        }
    }

    private void checkProjectAdminPermission(ErrorCollection errorCollection, Long projectId, User user)
    {
        boolean hasProjectAdminPermission = permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, projectManager.getProjectObj(projectId), user);
        if (!hasProjectAdminPermission)
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(translateKeyToMessage(KEY_USER_NO_PERMISSION_WITH_USER, user.getName()), ErrorCollection.Reason.FORBIDDEN);
            }
            else
            {
                errorCollection.addErrorMessage(translateKeyToMessage(KEY_USER_NO_PERMISSION, null), ErrorCollection.Reason.FORBIDDEN);
            }
        }
    }


    private void checkCanAccessComponentPermission(Handler handler, Long projectId, User user)
    {
        final boolean hasBrowsePermission = permissionManager.hasPermission(Permissions.BROWSE, projectManager.getProjectObj(projectId), user)
                || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, projectManager.getProjectObj(projectId), user)
                || permissionManager.hasPermission(Permissions.CREATE_ISSUE, projectManager.getProjectObj(projectId), user)
                || permissionManager.hasPermission(Permissions.EDIT_ISSUE, projectManager.getProjectObj(projectId), user)
                || permissionManager.hasPermission(Permissions.ADMINISTER, user);
        if (!hasBrowsePermission)
        {
            handler.addErrorKey(KEY_NO_SUCH_COMPONENT_WITH_ID, String.valueOf(projectId), ErrorCollection.Reason.NOT_FOUND);
        }

    }

    /****************************************
     *****   Validation Check Methods   *****
     ****************************************/

    /**
     * Validates project ID. Project ID is valid if not null.
     *
     * @param handler   validation and permission check handler
     * @param projectId project ID
     */
    protected void validateProjectId(Handler handler, Long projectId)
    {
        if (projectId == null)
        {
            handler.addErrorKey(KEY_PROJECT_ID_NULL, null, ErrorCollection.Reason.VALIDATION_FAILED);
        }
        else
        {
            try
            {
                if (projectManager.getProjectObj(projectId) == null)
                {
                    handler.addErrorKey(KEY_PROJECT_ID_NOT_FOUND, projectId.toString(), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
            catch (DataAccessException e)
            {
                LOG.error("Error encountered while attempting to find project with the ID '" + projectId + "'.", e);
                handler.addErrorKey(KEY_PROJECT_ID_NOT_FOUND, projectId.toString(), ErrorCollection.Reason.SERVER_ERROR);
            }
        }
    }

    /**
     * Validates newName. Name must be not null and unique.
     *
     * @param oldName   old name
     * @param newName   new name
     * @param projectId project ID
     * @param handler   validation and permission check handler
     */
    protected void validateName(Handler handler, String oldName, String newName, Long projectId)
    {
        if (StringUtils.isBlank(newName))
        {
            // New component name cannot be null or empty string
            handler.addErrorKey(FIELD_NAME, KEY_NAME_NOT_SET, null, ErrorCollection.Reason.VALIDATION_FAILED);
        }
        else
        if (!newName.equalsIgnoreCase(oldName) && projectComponentManager.containsName(newName, projectId))
        {
            // You must specify a UNIQUE newName for this component. New name is already being used
            handler.addErrorKey(FIELD_NAME, KEY_NAME_NOT_UNIQUE, newName, ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

    /**
     * Validates lead.
     * <p/>
     * For Standard & Professional - lead is always valid and will return null in all cases.
     * For Enterprise - lead is valid if null or a valid user.
     *
     * @param lead    lead
     * @param handler validation and permission check handler
     */
    protected void validateLead(Handler handler, String lead)
    {
        if (lead != null)
        {
            verifyUserExists(handler, lead);
        }
    }

    protected void validateAssigneeType(Handler handler, Long assigneeType)
    {
        if (assigneeType != null
            && !ComponentAssigneeTypes.isProjectDefault(assigneeType)
            && !ComponentAssigneeTypes.isProjectLead(assigneeType)
            && !ComponentAssigneeTypes.isComponentLead(assigneeType)
            && !ComponentAssigneeTypes.isUnassigned(assigneeType))
        {
            handler.addErrorKey(FIELD_ASSIGNEE_TYPE, KEY_ASSIGNEE_TYPE_INVALID, String.valueOf(assigneeType), ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

    /**
     * Verifies that the user with the name specified is a valid JIRA user.
     *
     * @param user user to verify
     * @throws IllegalArgumentException if user does not exist
     */
    protected void verifyUserExists(Handler handler, String user)
    {
        if (userManager.getUserByKey(user) == null)
        {
            // The user does not exist
            handler.addErrorKey(FIELD_COMPONENT_LEAD, KEY_USER_DOES_NOT_EXIST, user, ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

    /**
     * ****************************
     * ****   Utility Methods   *****
     * *****************************
     */

    /**
     * Translate the error message through the specified key with any errorValues to be
     * included in the error message - e.g. the id of a component that cannot be found.
     *
     * @param key the key for the error message
     * @return translated error message for the appropriate locale
     */
    private String translateKeyToMessage(String key, String errorValue)
    {
        if (errorValue == null)
        {
            return jiraAuthenticationContext.getI18nHelper().getText(key);
        }
        else
        {
            return jiraAuthenticationContext.getI18nHelper().getText(key, errorValue);
        }
    }

    /**
     * Encapsulation of a common strategy in each service method, to check
     * permissions, if no errors, to validate data, and if no errors to
     * complete the operation.
     */
    static abstract class Handler<T>
    {
        private SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        protected T result;

        private final ErrorCollection errorCollection;
        private final DefaultProjectComponentService componentService;

        public Handler(ErrorCollection errorKeys, DefaultProjectComponentService copmonentService)
        {
            this.errorCollection = errorKeys;
            this.componentService = copmonentService;
        }

        void checkPermissions()
        {

        }

        void validateData()
        {

        }

        abstract void executeOnSuccess();

        void addErrorKey(String key, String errorValue, ErrorCollection.Reason reason)
        {
            simpleErrorCollection.addErrorMessage(key, reason);
            if (errorCollection != null)
            {
                errorCollection.addErrorMessage(componentService.translateKeyToMessage(key, errorValue), reason);
            }
        }

        void addErrorKey(String fieldName, String key, String errorValue, ErrorCollection.Reason reason)
        {
            simpleErrorCollection.addError(fieldName, key, reason);
            if (errorCollection != null)
            {
                errorCollection.addError(fieldName, componentService.translateKeyToMessage(key, errorValue), reason);
            }
        }

        public T run()
        {
            checkPermissions();
            if (!simpleErrorCollection.hasAnyErrors())
            {
                validateData();
                if (!simpleErrorCollection.hasAnyErrors())
                {
                    executeOnSuccess();
                }
            }
            return result;
        }
    }

}
