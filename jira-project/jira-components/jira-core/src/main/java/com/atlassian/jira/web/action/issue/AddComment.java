/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.util.UtilDateTime;


public class AddComment extends AbstractCommentableIssue implements OperationContext
{
    protected final PermissionManager permissionManager;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public AddComment(SubTaskManager subTaskManager, FieldManager fieldManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, ProjectRoleManager projectRoleManager,
            CommentService commentService, PermissionManager permissionManager, UserUtil userUtil,
            IssueEventManager issueEventManager,
            IssueEventBundleFactory issueEventBundleFactory)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil);
        
        this.permissionManager = permissionManager;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    /**
     * Returns the name of the view to render the form to add a comment. This is only called when you right-click and
     * open in a new tab from the view issue page, it is not used to render the inline comment form.
     *
     * @return {@link #INPUT} if the issue exists and the user is authorised to comment on it; otherwise,
     * {@link #ERROR} is returned.
     */
    public String doDefault()
    {
        try
        {
            if (!isAbleToComment())
            {
                return ERROR;
            }
        }
        catch (IssueNotFoundException e)
        {
            //do not show error messages since the view will take care of it
            getErrorMessages().clear();
            return ERROR;
        }
        catch (IssuePermissionException ipe)
        {
            //do not show error messages since the view will take care of it
            getErrorMessages().clear();
            return ERROR;
        }

        return INPUT;
    }

    public boolean isAbleToComment()
    {
        return (getIssue() == null) ? permissionManager.hasPermission(Permissions.COMMENT_ISSUE, getIssueObject().getProjectObject(), getLoggedInApplicationUser())
                : permissionManager.hasPermission(Permissions.COMMENT_ISSUE, getIssueObject(), getLoggedInUser());
    }

    protected void doValidation()
    {
        try
        {
            super.doCommentValidation(false);
        }
        catch (IssueNotFoundException e)
        {
            // error message has been added in the super class
        }
        catch (IssuePermissionException e)
        {
            // error message has been added in the super class
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Comment comment = createComment();

        // dispatch event and update the issues updated field
        alertSystemOfComment(comment);

        //if a return url was explicitly specified (like when triggering this action from the Issue navigator
        //return a redirect to that return URL instead of the view issue page with the comment focused!
        if(StringUtils.isNotBlank(getReturnUrl()))
        {
            return returnCompleteWithInlineRedirect(getReturnUrl());
        }
        //
        // Its possible that the comment is in fact null for empty input.  While a bit strange, this is established
        // JIRA behaviour, probably because on transition you can have empty comments.  So we cater for it.
        //
        // I call it strange because all of the above code must be handling NULL comment objects.
        //
        final String browseIssue = "/browse/" + getIssue().getString("key");
        if (comment != null)
        {
            return returnComplete(browseIssue + "?focusedCommentId=" + comment.getId() +
                    "#comment-" + comment.getId());
        }
        else
        {
            return returnComplete(browseIssue);
        }
    }

    @VisibleForTesting
    void alertSystemOfComment(Comment comment) throws GenericEntityException
    {
        getIssueObject().setUpdated(UtilDateTime.nowTimestamp());
        getIssueObject().store();

        // fire a comment event
        Map<String, Object> params = createIssueEventParameters();
        issueEventManager.dispatchRedundantEvent(EventType.ISSUE_COMMENTED_ID, getIssueObject(), getLoggedInUser(), comment, null, null, params);

        IssueEventBundle issueCommentBundle = issueEventBundleFactory.createCommentAddedBundle(getIssueObject(), getLoggedInApplicationUser(), comment, params);
        issueEventManager.dispatchEvent(issueCommentBundle);
    }

    private Map<String, Object> createIssueEventParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("eventsource", IssueEventSource.ACTION);
        return params;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
