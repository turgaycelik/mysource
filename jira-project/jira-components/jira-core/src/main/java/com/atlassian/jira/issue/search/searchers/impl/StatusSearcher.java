package com.atlassian.jira.issue.search.searchers.impl;

import java.util.Collections;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.StatusIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.StatusSearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.IssueConstantSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.WorkflowManager;

@InjectableComponent
public class StatusSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearchInputTransformer searchInputTransformer;
    private final StatusSearchRenderer searchRenderer;
    private final SearcherInformation<SearchableField> searchInformation;

    public StatusSearcher(FieldVisibilityManager fieldVisibilityManager,
            final ConstantsManager constantsManager, JqlOperandResolver operandResolver,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine,
            WorkflowManager workflowManager, ProjectManager projectManager, StatusResolver statusResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        final IssueConstantInfoResolver<Status> constantInfoResolver = new IssueConstantInfoResolver<Status>(statusResolver);


        final SimpleFieldSearchConstants constants = SystemSearchConstants.forStatus();
        this.searchInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "navigator.filter.status", Collections.<Class<? extends FieldIndexer>>singletonList(StatusIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        this.searchRenderer = new StatusSearchRenderer(this.searchInformation.getNameKey(), constantsManager,
                velocityRequestContextFactory, applicationProperties, templatingEngine,
                fieldVisibilityManager, workflowManager, projectManager);

        this.searchInputTransformer = new IssueConstantSearchInputTransformer<Status>(constants.getJqlClauseNames(), constantInfoResolver, operandResolver, fieldFlagOperandRegistry, statusResolver);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return this.searchInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return this.searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return this.searchRenderer;
    }

}
