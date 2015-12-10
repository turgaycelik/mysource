package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * A factory for obtaining FieldScreenRenderers.
 */
@PublicApi
public interface FieldScreenRendererFactory
{
    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments.
     *
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    FieldScreenRenderer getFieldScreenRenderer(Issue issue, IssueOperation issueOperation);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments.
     * <p/>
     * Only the fields that match the passed predicate will be included in the returned {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}.
     *
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param predicate only fields that cause ths predicate to return true will be returned in the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    FieldScreenRenderer getFieldScreenRenderer(Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments. Only the fields
     * that match the passed predicate will be included in the returned {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param predicate only fields that cause ths predicate to return true will be returned in the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}
     *
     * @return a FieldScreenRenderer for the provided context.
     *
     * @deprecated Use {@link #getFieldScreenRenderer(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.operation.IssueOperation, com.atlassian.jira.util.Predicate)} instead. Since v6.2.
     */
    FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param onlyShownCustomFields if true will only return custom fields in the FieldScreenRenderer, otherwise
     * all fields will be returned.
     *
     * @return a FieldScreenRenderer for the provided context.
     *
     * @deprecated Use {@link #getFieldScreenRenderer(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.operation.IssueOperation)} instead. Use FieldPredicates.isCustomField() in the version that takes a predicate if you only want custom fields. Since v6.2.
     */
    FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields when transitioning through the passed workflow.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param actionDescriptor the current workflow action descriptor
     *
     * @return a FieldScreenRenderer for the provided context.
     *
     * @deprecated Use {@link #getFieldScreenRenderer(com.atlassian.jira.issue.Issue, com.opensymphony.workflow.loader.ActionDescriptor)} instead. Since v6.2.
     */
    FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, ActionDescriptor actionDescriptor);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields when transitioning through the passed workflow.
     *
     * @param issue the issue to be rendered.
     * @param actionDescriptor the current workflow action descriptor
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    FieldScreenRenderer getFieldScreenRenderer(Issue issue, ActionDescriptor actionDescriptor);

    /**
     * Used when need to populate a field without showing a screen - e.g. When using a UpdateIssueFieldFunction in workflow
     *
     * @param issue the currentIssue.
     *
     * @return a FieldScreenRenderer without any tabs.
     */
    FieldScreenRenderer getFieldScreenRenderer(Issue issue);

    /**
     * Get a renderer that can be used to render the fields when transitioning a collection of issues through workflow.
     *
     * @param issues the issues to be rendered.
     * @param actionDescriptor current workflow action descriptor
     *
     * @return a BulkFieldScreenRenderer - aggregates the tabs and fields for the specified collection of issues
     */
    FieldScreenRenderer getFieldScreenRenderer(Collection<Issue> issues, ActionDescriptor actionDescriptor);

    /**
     * Returns a {@link FieldScreenRenderer} that represents a 'field screen' with the fields the ids of which
     * are in fieldIds. The returned Field Renderer places all given fields on one tab.
     *
     * @param fieldIds the fields to create the renderer for.
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     *
     * @return a FieldScreenRenderer for the passed fields.
     */
    FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, User remoteUser, Issue issue, IssueOperation issueOperation);
}
