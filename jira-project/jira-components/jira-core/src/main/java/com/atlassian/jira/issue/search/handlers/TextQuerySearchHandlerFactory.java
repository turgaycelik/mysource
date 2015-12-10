package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.impl.TextQuerySearcher;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.context.AllTextClauseContextFactory;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.AllTextClauseQueryFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.AllTextValidator;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.List;

import static com.atlassian.jira.jql.permission.DefaultClausePermissionHandler.NOOP_CLAUSE_PERMISSION_HANDLER;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates a SearchHandler for text ~ searches.
 *
 * @since v5.2
 */
public class TextQuerySearchHandlerFactory implements SearchHandlerFactory
{
    private final ClauseValidator clauseValidator;
    private final ClauseContextFactory clauseContextFactory;
    private JqlOperandResolver operandResolver;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final VelocityTemplatingEngine templatingEngine;
    private final ClauseQueryFactory clauseQueryFactory;
    private final ClauseInformation clauseInfo;

    public TextQuerySearchHandlerFactory(AllTextValidator clauseValidator, AllTextClauseContextFactory clauseContextFactory,
            CustomFieldManager customFieldManager, SearchHandlerManager searchHandlerManager, JqlOperandResolver operandResolver,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine)
    {
        this.operandResolver = operandResolver;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.templatingEngine = templatingEngine;
        this.clauseContextFactory = notNull("clauseContextFactory", clauseContextFactory);
        this.clauseQueryFactory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager);
        this.clauseInfo = SystemSearchConstants.forAllText();
        this.clauseValidator = notNull("clauseValidator", clauseValidator);
    }

    @Override
    public SearchHandler createHandler(SearchableField field)
    {
        return createHandler();
    }

    public SearchHandler createHandler()
    {
        TextQuerySearcher searcher = new TextQuerySearcher(operandResolver, velocityRequestContextFactory, applicationProperties, templatingEngine);
        ClauseHandler clauseHandler = new DefaultClauseHandler(clauseInfo, clauseQueryFactory, clauseValidator,
                NOOP_CLAUSE_PERMISSION_HANDLER, clauseContextFactory);
        SearchHandler.SearcherRegistration searcherRegistration = new SearchHandler.SearcherRegistration(searcher, clauseHandler);
        final List<FieldIndexer> relatedIndexers = searcher.getSearchInformation().getRelatedIndexers();
        return new SearchHandler(relatedIndexers, searcherRegistration);
    }


}
