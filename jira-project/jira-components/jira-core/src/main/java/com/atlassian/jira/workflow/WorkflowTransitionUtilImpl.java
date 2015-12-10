package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.WorkflowIssueOperationImpl;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class WorkflowTransitionUtilImpl implements WorkflowProgressAware, WorkflowTransitionUtil
{
    private final ErrorCollection errorCollection = new SimpleErrorCollection();
    private final JiraAuthenticationContext authenticationContext;
    private final WorkflowManager workflowManager;
    private final PermissionManager permissionManager;

    private MutableIssue issue;
    private Project project;
    private int actionId;
    private ActionDescriptor actionDescriptor;

    private Map<String, Object> params = Maps.newHashMap();

    // The user who the workflow transition will be executed as
    @Nullable
    private ApplicationUser remoteUser;
    private FieldScreenRenderer fieldScreenRenderer;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final CommentService commentService;
    private final I18nHelper.BeanFactory i18nFactory;
    private final Map additionalInputs = Maps.newHashMap();

    public WorkflowTransitionUtilImpl(final JiraAuthenticationContext authenticationContext,
            final WorkflowManager workflowManager, final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final CommentService commentService,
            final I18nHelper.BeanFactory i18nFactory)
    {
        this.authenticationContext = authenticationContext;
        this.workflowManager = workflowManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.commentService = commentService;
        this.i18nFactory = i18nFactory;
        this.remoteUser = authenticationContext.getUser();
    }

    @Override
    public MutableIssue getIssue()
    {
        return issue;
    }

    @Override
    public void setIssue(final MutableIssue issue)
    {
        this.issue = issue;
    }

    private String getComment()
    {
        return (String) params.get(FIELD_COMMENT);
    }

    // This is really getGroupCommentLevel
    private String getCommentLevel()
    {
        return (String) params.get(FIELD_COMMENT_LEVEL);
    }

    private String getCommentRoleLevel()
    {
        return (String) params.get(FIELD_COMMENT_ROLE_LEVEL);
    }

    @Override
    public Project getProject()
    {
        return getProjectObject();
    }

    @Override
    public Project getProjectObject()
    {
        if (project == null)
        {
            project = issue.getProjectObject();
        }

        return project;
    }

    /**
     * @deprecated Use {@link #getRemoteApplicationUser()} instead. Since v6.2.3.
     */
    @Override
    public User getRemoteUser()
    {
        return ApplicationUsers.toDirectoryUser(getRemoteApplicationUser());
    }

    @Override
    public ApplicationUser getRemoteApplicationUser()
    {
        return remoteUser;
    }

    public int getAction()
    {
        return actionId;
    }

    @Override
    public void setAction(final int action)
    {
        actionId = action;
    }

    public ActionDescriptor getActionDescriptor()
    {
        if (actionDescriptor == null)
        {
            actionDescriptor = retrieveActionDescriptorWithPermissionCheck();
        }

        return actionDescriptor;
    }

    private ActionDescriptor retrieveActionDescriptorWithPermissionCheck()
    {
        try
        {
            if (!hasTransitionPermission())
            {
                throw new IllegalArgumentException(String.format(
                        "User %s doesn't have permission transition for issue %s.", getUserKey(), getIssue().getKey()));
            }

            return retrieveActionDescriptor();
        }
        catch (final WorkflowException e)
        {
            throw new IllegalArgumentException("Cannot find workflow transition with id '" + actionId + "'.", e);
        }
    }

    private ActionDescriptor retrieveActionDescriptor()
    {
        // Impersonate user just in case if DefaultJiraWorkflow or others use JiraAuthenticationContext ;)
        final ActionDescriptor retrievedActionDescriptor = impersonateUser(new Supplier<ActionDescriptor>()
        {
            @Override
            public ActionDescriptor get()
            {
                return workflowManager.getWorkflow(getIssue()).getDescriptor().getAction(actionId);
            }
        });

        if (retrievedActionDescriptor == null)
        {
            throw new IllegalArgumentException("No workflow action with id '" + actionId + "' available for issue " + getIssue().getKey());
        }
        else
        {
            return retrievedActionDescriptor;
        }
    }

    @Override
    public void addErrorMessage(final String error)
    {
        errorCollection.addErrorMessage(error);
    }

    @Override
    public void addError(final String name, final String error)
    {
        errorCollection.addError(name, error);
    }

    @SuppressWarnings ("unchecked")
    @Override
    public Map getAdditionalInputs()
    {
        final Map map = new HashMap(additionalInputs.size() + 3);
        map.putAll(additionalInputs);

        // Only supply fields that have been updated
        if (fieldUpdated(FIELD_COMMENT))
        {
            map.put(FIELD_COMMENT, getComment());
            map.put(FIELD_COMMENT_LEVEL, getCommentLevel());
            map.put(CommentSystemField.PARAM_ROLE_LEVEL, getCommentRoleLevel());
        }

        WorkflowFunctionUtils.populateParamsWithUser(map, getUserKey());

        return map;
    }

    @SuppressWarnings ("unchecked")
    @Override
    public void addAdditionalInput(final Object key, final Object value)
    {
        additionalInputs.put(key, value);
    }

    /**
     * @deprecated Use {@link #getUserKey()} instead. Since v6.0.
     */
    @Override
    public String getUsername()
    {
        return getUsername(getRemoteApplicationUser());
    }

    @Nullable
    private String getUsername(@Nullable final ApplicationUser user)
    {
        return user != null ? user.getUsername() : null;
    }

    /**
     * @deprecated Use {@link #setUserkey(String)} instead. Since v6.0.
     */
    @Override
    public void setUsername(final String username)
    {
        remoteUser = ComponentAccessor.getUserManager().getUserByName(username);
    }

    @Override
    public String getUserKey()
    {
        return ApplicationUsers.getKeyFor(getRemoteApplicationUser());
    }

    @Override
    public void setUserkey(final String userkey)
    {
        remoteUser = ApplicationUsers.byKey(userkey);
    }

    @Override
    public ErrorCollection validate()
    {
        // We can't be sure that our managers or other creatures doesn't use JiraAuthenticationContext. Let's impersonate!
        return impersonateUser(new Supplier<ErrorCollection>()
        {
            @Override
            public ErrorCollection get()
            {
                validateComment();

                validateTransitionPermission();

                if (errorCollection.hasAnyErrors())
                {
                    return errorCollection;
                }

                validateFieldsParams();

                return errorCollection;
            }
        });
    }

    private void validateFieldsParams()
    {
        for (final FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
        {
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(getIssue()))
                {
                    final OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                    // JRA-16112 - This is a hack that is here because the resolution field is "special". You can not
                    // make the resolution field required and therefore by default the FieldLayoutItem for resolution
                    // returns false for the isRequired method. This is so that you can not make the resolution field
                    // required for issue creation. HOWEVER, whenever the resolution system field is shown it is
                    // required because the edit template does not provide a none option and indicates that it is
                    // required. THEREFORE, when the field is included on a transition screen we will do a special
                    // check to make the FieldLayoutItem claim it is required IF we run into the resolution field.
                    final boolean isResolutionField = IssueFieldConstants.RESOLUTION.equals(orderableField.getId());
                    final FieldScreenRenderLayoutItem itemWithHack =
                            isResolutionField ? makeRequired(fieldScreenRenderLayoutItem) : fieldScreenRenderLayoutItem;

                    orderableField.validateParams(getOperationContext(), errorCollection, getI18n(), getIssue(), itemWithHack);
                }
            }
        }
    }

    private FieldScreenRenderLayoutItem makeRequired(final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        return new FieldScreenRenderLayoutItemImpl(fieldScreenRenderLayoutItem.getFieldScreenLayoutItem(), fieldScreenRenderLayoutItem.getFieldLayoutItem())
        {
            public boolean isRequired()
            {
                return true;
            }
        };
    }

    private I18nHelper getI18n()
    {
        return i18nFactory.getInstance(getRemoteApplicationUser());
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getIssue(), getActionDescriptor());
        }

        return fieldScreenRenderer;
    }

    private void validateTransitionPermission()
    {
        if (!hasTransitionPermission())
        {
            addErrorMessage(getI18nMessageWithUser("admin.errors.user.does.not.have.transition.permission"));
        }
    }


    private void validateComment()
    {
        // Check if we have a comment
        if (fieldUpdated(FIELD_COMMENT))
        {
            // If so if the user can comment on the issue
            final ApplicationUser remoteApplicationUser = getRemoteApplicationUser();
            if (permissionManager.hasPermission(ProjectPermissions.ADD_COMMENTS, getIssue(), remoteApplicationUser))
            {
                final Visibility visibility = Visibilities.fromGroupAndStrRoleId(getCommentLevel(), getCommentRoleLevel());
                commentService.isValidCommentVisibility(remoteApplicationUser, getIssue(), visibility, errorCollection);
                if (!fieldUpdated(FIELD_COMMENT_LEVEL) && !fieldUpdated(FIELD_COMMENT_ROLE_LEVEL))
                {
                    setCommentLevel(null);
                }
            }
            else
            {
                errorCollection.addErrorMessage(getI18nMessageWithUser("admin.errors.user.does.not.have.permission"));
            }
        }
    }

    private String getI18nMessageWithUser(final String i18nKey)
    {
        final ApplicationUser remoteApplicationUser = getRemoteApplicationUser();
        final I18nHelper i18n = getI18n();
        final String username = (remoteApplicationUser != null)
                ? i18n.getText("admin.errors.user", "'" + remoteApplicationUser.getName() + "'")
                : i18n.getText("admin.errors.anonymous.user");

        return i18n.getText(i18nKey, username);
    }

    private void setCommentLevel(final String commentLevel)
    {
        params.put(FIELD_COMMENT_LEVEL, commentLevel);
    }

    public ErrorCollection progress()
    {
        // Only update issue if transition has a screen
        if (hasScreen())
        {
            for (final FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
            {
                for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
                {
                    if (fieldScreenRenderLayoutItem.isShow(getIssue()))
                    {
                        fieldScreenRenderLayoutItem.getOrderableField().updateIssue(fieldScreenRenderLayoutItem.getFieldLayoutItem(), getIssue(), params);
                    }
                }
            }
        }

        workflowManager.doWorkflowAction(this);

        return errorCollection;
    }

    @Override
    public void setParams(final Map params)
    {
        //noinspection unchecked
        this.params = params;
    }

    @Override
    public boolean hasScreen()
    {
        return StringUtils.isNotBlank(getActionDescriptor().getView());
    }

    private boolean fieldUpdated(final String fieldId)
    {
        return params.containsKey(fieldId);
    }

    private OperationContext getOperationContext()
    {
        return new OperationContextImpl(new WorkflowIssueOperationImpl(getActionDescriptor()), params);
    }

    private boolean hasTransitionPermission() {
        return permissionManager.hasPermission(ProjectPermissions.TRANSITION_ISSUES, getIssue(), getRemoteApplicationUser());
    }

    private <T> T impersonateUser(final Supplier<T> supplier)
    {
        final ApplicationUser oldUser = authenticationContext.getUser();
        authenticationContext.setLoggedInUser(getRemoteApplicationUser());
        try
        {
            return supplier.get();
        }
        finally
        {
            authenticationContext.setLoggedInUser(oldUser);
        }
    }
}
