package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.AffectedVersionsIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.AffectedVersionRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

/**
 * A searcher for the affected version field.
 */
public class AffectedVersionsSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public AffectedVersionsSearcher(VersionResolver versionResolver,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry,
            ProjectManager projectManager, VersionManager versionManager, FieldVisibilityManager fieldVisibilityManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, PermissionManager permissionManager)
    {
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(DocumentConstants.ISSUE_VERSION,
                "common.filters.matchesversions", Collections.<Class<? extends FieldIndexer>>singletonList(AffectedVersionsIndexer.class),
                fieldReference, SearcherGroupType.PROJECT);

        final SimpleFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forAffectedVersion();
        VersionIndexInfoResolver indexInfoResolver = new VersionIndexInfoResolver(versionResolver);

        this.searchInputTransformer = new VersionSearchInputTransformer(constants.getJqlClauseNames(), constants.getUrlParameter(),
                operandResolver, fieldFlagOperandRegistry, versionResolver);
        this.searchRenderer = new AffectedVersionRenderer(projectManager, versionManager, fieldVisibilityManager,
                velocityRequestContextFactory, applicationProperties,
                templatingEngine, permissionManager, this.searcherInformation.getNameKey());
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
