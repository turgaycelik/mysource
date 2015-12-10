package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Map;

/**
 * Builder for {@link com.atlassian.jira.rest.v2.issue.FieldMetaBean} instances, in the context of meta data for creating issues.
 *
 * @since v5.0
 */
public class EditMetaFieldBeanBuilder extends AbstractMetaFieldBeanBuilder
{
    private final OperationContext operationContext = new EditIssueOperationContext();
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldManager fieldManager;

    public EditMetaFieldBeanBuilder(final FieldLayoutManager fieldLayoutManager, final Project project,
            final Issue issue, final IssueType issueType, final User user, final VersionBeanFactory versionBeanFactory,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ContextUriInfo contextUriInfo,
            final JiraBaseUrls baseUrls, final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final FieldManager fieldManager)
    {
        super(fieldLayoutManager, project, issue, issueType, user, versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls, null);
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
    }

    @Override
    protected void addAdditionalFields(final Map<String, FieldMetaBean> fields)
    {
        OrderableField field = (OrderableField) fieldManager.getField(SystemSearchConstants.forComments().getFieldId());
        if (field.isShown(issue))
        {
            String id = field.getId();
            if (includeFields == null || includeFields.included(field))
            {
                final FieldMetaBean fieldMetaBean = getFieldMetaBean(false, field);
                fields.put(id, fieldMetaBean);
            }
        }
    }

    @Override
    public OperationContext getOperationContext()
    {
        return operationContext;
    }

    @Override
    public boolean hasPermissionToPerformOperation()
    {
        return permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }

    @Override
    FieldScreenRenderer getFieldScreenRenderer(final Issue issue)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.EDIT_ISSUE_OPERATION);
    }
}
