package com.atlassian.jira.issue.fields.screen;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.tab.FieldScreenTabRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of the FieldScreenRendererFactory.
 */
public class FieldScreenRendererFactoryImpl implements FieldScreenRendererFactory
{
    private final BulkFieldScreenRendererFactory bulkRendererFactory;
    private final StandardFieldScreenRendererFactory rendererFactory;

    public FieldScreenRendererFactoryImpl(FieldManager fieldManager, FieldLayoutManager fieldLayoutManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            FieldScreenManager fieldScreenManager, HackyFieldRendererRegistry hackyFieldRendererRegistry, FieldScreenTabRendererFactory fieldScreenTabRendererFactory)
    {
        this (new BulkFieldScreenRendererFactory(fieldManager, fieldLayoutManager, hackyFieldRendererRegistry),
                new StandardFieldScreenRendererFactory(fieldManager, fieldLayoutManager, issueTypeScreenSchemeManager, fieldScreenManager, fieldScreenTabRendererFactory));
    }

    FieldScreenRendererFactoryImpl(BulkFieldScreenRendererFactory bulkRendererFactory, StandardFieldScreenRendererFactory rendererFactory)
    {
        this.bulkRendererFactory = bulkRendererFactory;
        this.rendererFactory = rendererFactory;
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(final Issue issue, final IssueOperation issueOperation)
    {
        return getFieldScreenRenderer(issue, issueOperation, Predicates.<Field>truePredicate());
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
    {
        return rendererFactory.createFieldScreenRenderer(issue, issueOperation, predicate);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields)
    {
        return getFieldScreenRenderer(issue, issueOperation, onlyShownCustomFields ? FieldPredicates.isCustomField() : Predicates.<Field>truePredicate());
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
    {
        return rendererFactory.createFieldScreenRenderer(issue, issueOperation, predicate);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, ActionDescriptor actionDescriptor)
    {
        return rendererFactory.createFieldScreenRenderer(issue, actionDescriptor);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(final Issue issue, final ActionDescriptor actionDescriptor)
    {
        return rendererFactory.createFieldScreenRenderer(issue, actionDescriptor);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(Issue issue)
    {
        return rendererFactory.createFieldScreenRenderer(issue);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        return bulkRendererFactory.createRenderer(issues, actionDescriptor);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, User remoteUser, Issue issue, IssueOperation issueOperation)
    {
        return rendererFactory.createFieldScreenRenderer(fieldIds, issue, issueOperation);
    }
}
