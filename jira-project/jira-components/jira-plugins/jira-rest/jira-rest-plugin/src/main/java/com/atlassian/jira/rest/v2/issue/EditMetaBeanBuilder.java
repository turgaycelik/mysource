package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

/**
 * Builder for {@link com.atlassian.jira.rest.v2.issue.CreateMetaBean} instances.
 *
 * @since v5.0
 */
public class EditMetaBeanBuilder
{
    private final JiraAuthenticationContext authContext;
    private final FieldLayoutManager fieldLayoutManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ContextUriInfo contextUriInfo;
    private final VersionBeanFactory versionBeanFactory;
    private final JiraBaseUrls baseUrls;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldManager fieldManager;

    private Issue issue;
    private IncludedFields includeFields;

    public EditMetaBeanBuilder(
            final JiraAuthenticationContext authContext,
            final FieldLayoutManager fieldLayoutManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ContextUriInfo contextUriInfo,
            final VersionBeanFactory versionBeanFactory,
            final JiraBaseUrls baseUrls,
            final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldManager fieldManager)
    {
        this.authContext = authContext;
        this.fieldLayoutManager = fieldLayoutManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
        this.versionBeanFactory = versionBeanFactory;
        this.baseUrls = baseUrls;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
    }

    public EditMetaBeanBuilder issue(final Issue issue)
    {
        this.issue = issue;
        return this;
    }

    public EditMetaBeanBuilder fieldsToInclude(IncludedFields includeFields)
    {
        this.includeFields = includeFields;
        return this;
    }

    public EditMetaBean build()
    {
        EditMetaFieldBeanBuilder fieldBuilder = new EditMetaFieldBeanBuilder(fieldLayoutManager, issue.getProjectObject(), issue, issue.getIssueTypeObject(), authContext.getLoggedInUser(), versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls, permissionManager, fieldScreenRendererFactory, fieldManager);
        fieldBuilder.fieldsToInclude(includeFields);
        return new EditMetaBean(issue, fieldBuilder);
    }

}
