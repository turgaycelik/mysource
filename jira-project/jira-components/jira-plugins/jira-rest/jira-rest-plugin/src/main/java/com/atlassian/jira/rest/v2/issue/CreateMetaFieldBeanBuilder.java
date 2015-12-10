package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Builder for {@link FieldMetaBean} instances, in the context of meta data for creating issues.
 *
 * @since v5.0
 */
public class CreateMetaFieldBeanBuilder extends AbstractMetaFieldBeanBuilder
{
    private final OperationContext operationContext = new CreateIssueOperationContext();
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final JiraAuthenticationContext authContext;
    private final FieldManager fieldManager;

    public CreateMetaFieldBeanBuilder(final FieldLayoutManager fieldLayoutManager, final Project project,
            final Issue issue, final IssueType issueType, final User user, final VersionBeanFactory versionBeanFactory,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ContextUriInfo contextUriInfo,
            final JiraBaseUrls baseUrls, final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final JiraAuthenticationContext authContext,
            final FieldManager fieldManager, final DefaultFieldMetaBeanHelper defaultFieldHelper)
    {
        super(fieldLayoutManager, project, issue, issueType, user, versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls, defaultFieldHelper);
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.authContext = authContext;
        this.fieldManager = fieldManager;
    }

    @Override
    public OperationContext getOperationContext()
    {
        return operationContext;
    }

    @Override
    public Map<String, FieldMetaBean> build()
    {
        final Map<String, FieldMetaBean> fields = super.build();

        // Add 'Project' to the CREATE fields list, since it isn't in the field layout, but in REST it is "required" for create
        fields.put(IssueFieldConstants.PROJECT, createProjectFieldMetaBean());

        // The issue type is required, so let's add it if it's missing (due to some insane screen configuration)
        if (!fields.containsKey(IssueFieldConstants.ISSUE_TYPE)) {
            fields.put(IssueFieldConstants.ISSUE_TYPE, createIssueTypeFieldMetaBean());
        }

        // If this is for a subtask then add the "parent" pseudo field
        if (issueType.isSubTask())
        {
            fields.put("parent", createParentFieldMetaBean());
        }

        return fields;
    }

    private FieldMetaBean createParentFieldMetaBean()
    {
        return new FieldMetaBean(true, false, JsonTypeBuilder.system("issuelink", "parent"),
                authContext.getI18nHelper().getText("issue.field.parent"), null,
                Collections.singletonList(StandardOperation.SET.getName()), null);
    }

    private FieldMetaBean createIssueTypeFieldMetaBean()
    {
        return getFieldMetaBean(true, fieldManager.getOrderableField(IssueFieldConstants.ISSUE_TYPE));
    }

    private FieldMetaBean createProjectFieldMetaBean()
    {
        return new FieldMetaBean(true, false, ProjectSystemField.getJsonType(),
                authContext.getI18nHelper().getText(ProjectSystemField.PROJECT_NAME_KEY), null,
                Collections.singletonList(StandardOperation.SET.getName()), Collections.singletonList(ProjectJsonBean.shortBean(project, baseUrls)));
    }

    @Override
    public boolean hasPermissionToPerformOperation()
    {
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, issue, user);
    }

    @Override
    FieldScreenRenderer getFieldScreenRenderer(Issue issue)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.CREATE_ISSUE_OPERATION);
    }
}
