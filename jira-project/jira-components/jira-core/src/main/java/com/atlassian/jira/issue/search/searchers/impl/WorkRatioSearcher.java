package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.WorkRatioIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.WorkRatioSearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.WorkRatioSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

public class WorkRatioSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public WorkRatioSearcher(final JqlOperandResolver operandResolver, final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory velocityRenderContext, final VelocityTemplatingEngine templatingEngine,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forWorkRatio();
        final WorkRatioSearcherConfig config = new WorkRatioSearcherConfig(constants.getJqlClauseNames().getPrimaryName());
        final String nameKey = "navigator.filter.workratio.limits";

        this.searcherInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(),
                nameKey, Collections.<Class<? extends FieldIndexer>>singletonList(WorkRatioIndexer.class),
                fieldReference, SearcherGroupType.WORK);
        this.searchInputTransformer = new WorkRatioSearchInputTransformer(constants, config, operandResolver);
        this.searchRenderer = new WorkRatioSearchRenderer(velocityRenderContext, applicationProperties,
                templatingEngine, constants, nameKey, fieldVisibilityManager, config);
    }

    public final SearcherInformation<SearchableField> getSearchInformation()
    {
        return searcherInformation;
    }

    public final SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    public final SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }

}
