package com.atlassian.jira.web.action.issue;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.CreateValidationResult;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkUICreateEvent;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BrowserUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for an issue link action.
 *
 * @since v5.0
 */
public abstract class AbstractIssueLinkAction extends AbstractCommentableIssue implements OperationContext
{
    protected CreateValidationResult validationResult;

    protected final RemoteIssueLinkService remoteIssueLinkService;
    protected final EventPublisher eventPublisher;

    private boolean requiresCredentials;

    public AbstractIssueLinkAction(
            final SubTaskManager subTaskManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldManager fieldManager,
            final ProjectRoleManager projectRoleManager,
            final CommentService commentService,
            final UserUtil userUtil,
            final RemoteIssueLinkService remoteIssueLinkService,
            final EventPublisher eventPublisher)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil);
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Validate that the user has permission to link issues, and that the comment field is valid. Override this method
     * to perform validation specific to the concrete action. It is recommended that this method be invoked by any
     * overriding methods.
     */
    @Override
    protected void doValidation()
    {
        if (!hasIssuePermission(Permissions.LINK_ISSUE, getIssueObject()))
        {
            addErrorMessage(getText("linkissue.error.nopermission"));
        }

        // Validate comment
        super.doValidation();
    }

    /**
     * The "default" command. Checks that the current issue exists and that the user has permission to view it.
     *
     * @return INPUT if success, ERROR if otherwise
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception
    {
        try
        {
            getIssueObject();
        }
        catch (IssueNotFoundException e)
        {
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        return super.doDefault();
    }

    /**
     * Creates the link and publishes an event for the creation
     *
     * @return create result
     */
    public RemoteIssueLinkService.RemoteIssueLinkResult createLink()
    {
        return createLink(validationResult);
    }

    /**
     * Creates the link and publishes an event for the creation
     *
     * @param validationResult the CreateValidationResult to be used to create the link
     * @return create result
     */
    public RemoteIssueLinkService.RemoteIssueLinkResult createLink(final CreateValidationResult validationResult)
    {
        final RemoteIssueLinkService.RemoteIssueLinkResult result = remoteIssueLinkService.create(getLoggedInUser(), validationResult);

        final RemoteIssueLink link = result.getRemoteIssueLink();
        eventPublisher.publish(new RemoteIssueLinkUICreateEvent(link));

        return result;
    }

    /**
     * Returns true if the contents of the action should be shown, false if otherwise.
     *
     * @return true if the contents of the action should be shown, false if otherwise
     */
    public boolean isValidToView()
    {
        try
        {
            // The action should be shown if the issue exists and the user has permission to link issues
            if (isIssueExists() && hasIssuePermission(Permissions.LINK_ISSUE, getIssueObject()))
            {
                return true;
            }
        }
        catch (IssueNotFoundException e)
        {
            return false;
        }
        catch (IssuePermissionException e)
        {
            return false;
        }

        return false;
    }

    /**
     * Returns the URL to redirect to after successfully creating the issue link.
     *
     * @return the URL to redirect to after successfully creating the issue link
     */
    protected String getRedirectUrl()
    {
        return "/browse/" + getIssue().getString("key") + "#linkingmodule";
    }

    protected void handleCredentialsRequired()
    {
        // Required so that the error case is handled correctly
        addError("non-existent", "");
        requiresCredentials = true;
    }

    @SuppressWarnings ("unused")
    public boolean isRequiresCredentials()
    {
        return requiresCredentials;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }

    @SuppressWarnings ("unused")
    public String getCommentSectionHtml()
    {
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = getFieldScreenRendererLayoutItemForField((OrderableField) getField("comment"));
        FieldLayoutItem fieldLayoutItem = fieldScreenRenderLayoutItem.getFieldLayoutItem();
        return fieldLayoutItem.getOrderableField().getEditHtml(fieldLayoutItem, this, this, getIssueObject(), getDisplayParams());
    }

    @SuppressWarnings ("unused")
    public static String getModifierKey()
    {
        return BrowserUtils.getModifierKey();
    }

    @SuppressWarnings ("unused")
    public static KeyboardShortcutManager.Context getKeyboardShortcutContext()
    {
        return KeyboardShortcutManager.Context.issuenavigation;
    }
}
