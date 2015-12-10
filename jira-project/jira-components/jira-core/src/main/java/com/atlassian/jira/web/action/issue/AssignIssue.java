package com.atlassian.jira.web.action.issue;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

import com.opensymphony.util.TextUtils;

import webwork.action.ActionContext;

public class AssignIssue extends AbstractCommentableAssignableIssue implements OperationContext
{
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final FieldManager fieldManager;
    private final IssueManager issueManager;

    private FieldScreenRenderer fieldScreenRenderer;

    public AssignIssue(final SubTaskManager subTaskManager, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldLayoutManager fieldLayoutManager, final FieldManager fieldManager,
            final CommentService commentService, final IssueManager issueManager, final UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, commentService, userUtil);
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldManager = fieldManager;
        this.issueManager = issueManager;
    }

    public String doDefault() throws Exception
    {
        try
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    if (fieldScreenRenderLayoutItem.isShow(getIssueObject()))
                    {
                        fieldScreenRenderLayoutItem.populateFromIssue(getFieldValuesHolder(), getIssueObject());
                    }
                }
            }

            return super.doDefault();
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }
    }

    public void doValidation()
    {
        try
        {
            // validate the comment params
            doCommentValidation(true);

            // validate assignee params
            OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
            field.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
            field.validateParams(this, this, this, getIssueObject(), getFieldScreenRendererLayoutItemForField(field));

            if(!issueManager.isEditable(getIssueObject()))
            {
                addErrorMessage(getText("editissue.error.no.edit.workflow"));
            }

            // Do extra validation that only needs to occur on the assign issue screen
            if (!TextUtils.stringSet(getAssignee()))
            {
                if (!TextUtils.stringSet(getIssue().getString("assignee")))
                {
                    addError("assignee", getText("assign.error.alreadyunassigned"));
                }
            }
            else
            {
                // Be aware that getIssue().getString("assignee") is the user key but this action takes a username
                ApplicationUser currentAssignee = userUtil.getUserByKey(getIssue().getString("assignee"));
                if (currentAssignee != null && getAssignee().equals(currentAssignee.getUsername()))
                {
                    addError("assignee", getText("assign.error.alreadyassigned", EasyList.build(currentAssignee.getDisplayName(), currentAssignee.getUsername())));
                }
            }
        }
        catch (Exception e)
        {
            log.error(e, e);
            addError("assignee", getText("assign.error.userdoesnotexist", getAssignee()));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        MutableIssue issue = getIssueObject();

        // update the assignee system field
        OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field);
        field.updateIssue(fieldLayoutItem, issue, getFieldValuesHolder());

        // This hack is here until the comment field becomes placeable on screens by the users
        OrderableField commentField = (OrderableField) fieldManager.getField(IssueFieldConstants.COMMENT);
        FieldLayoutItem fieldLayoutItem2 = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(commentField);
        commentField.updateIssue(fieldLayoutItem2, issue, getFieldValuesHolder());

        issueManager.updateIssue(getLoggedInUser(), issue, EventDispatchOption.ISSUE_ASSIGNED, true);

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return getRedirect("/browse/" + issue.getKey());
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getIssueObject(), IssueOperations.EDIT_ISSUE_OPERATION);
        }

        return fieldScreenRenderer;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
