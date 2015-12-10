package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UpdateFieldsHelperBeanImpl implements UpdateFieldsHelperBean
{
    private static final Logger log = Logger.getLogger(UpdateFieldsHelperBeanImpl.class);

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private FieldScreenRendererFactory fieldScreenRendererFactory;

    public UpdateFieldsHelperBeanImpl(PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, FieldScreenRendererFactory fieldScreenRendererFactory)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
    }

    public boolean isEditable(Issue issue)
    {
        // preconditions
        if (issue == null)
        {
            throw new IssueNotFoundException("Issue unexpectedly null");
        }

        boolean hasPermission = permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, authenticationContext.getLoggedInUser());
        if (hasPermission)
        {
            return issue.isEditable();
        }
        return false;
    }

    public List getFieldsForEdit(User user, Issue issueObject)
    {
        final List fields = new ArrayList();

        FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);

        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    fields.add(fieldScreenRenderLayoutItem.getOrderableField());
                }
            }
        }
        return fields;
    }

    public boolean isFieldValidForEdit(User user, String fieldId, Issue issueObject)
    {
        if (fieldId != null)
        {
            FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(user, issueObject);
            for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
                {
                    if (fieldScreenRenderLayoutItem.isShow(issueObject) && fieldId.equals(fieldScreenRenderLayoutItem.getOrderableField().getId()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private FieldScreenRenderer getFieldScreenRenderer(User user, final Issue issueObject)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issueObject, IssueOperations.EDIT_ISSUE_OPERATION, false);
    }
}
