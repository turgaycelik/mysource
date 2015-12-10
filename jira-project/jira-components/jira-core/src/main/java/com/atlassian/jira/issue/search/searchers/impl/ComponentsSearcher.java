package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ComponentsIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.ComponentSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

/**
 * A searcher for the component system field.
 */
public class ComponentsSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    final private SearcherInformation<SearchableField> searcherInformation;
    final private SearchInputTransformer searchInputTransformer;
    final private SearchRenderer searchRenderer;

    public ComponentsSearcher(VelocityTemplatingEngine templatingEngine,
            VelocityRequestContextFactory contextFactory,
            FieldVisibilityManager fieldVisibilityManager,
            ComponentResolver componentResolver,
            ApplicationProperties applicationProperties,
            ProjectComponentManager componentManager,
            ProjectManager projectManager,
            JqlOperandResolver operandResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry,
            PermissionManager permissionManager)
    {
        final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();

        searcherInformation = new GenericSearcherInformation<SearchableField>(searchConstants.getSearcherId(), "common.filters.incomponents",
                Collections.<Class<? extends FieldIndexer>>singletonList(ComponentsIndexer.class),
                fieldReference, SearcherGroupType.PROJECT);

        searchRenderer = new ComponentSearchRenderer(
                applicationProperties, fieldVisibilityManager, componentManager,
                projectManager, searchConstants,
                searcherInformation.getNameKey(), templatingEngine,
                contextFactory, permissionManager);

        searchInputTransformer = new ComponentSearchInputTransformer(searchConstants.getJqlClauseNames(),
                searchConstants.getUrlParameter(), operandResolver, fieldFlagOperandRegistry, componentResolver);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }

}
