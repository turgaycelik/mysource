package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.DefaultValuesGeneratingClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Creates a {@link com.atlassian.jira.issue.search.SearchHandler} for fields that have a single {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}
 *  that in turn has a single {@link com.atlassian.jira.jql.ClauseHandler}. This is the case for all the system fields.
 *
 * @since v4.0
 */
abstract class SimpleSearchHandlerFactory implements SearchHandlerFactory
{
    private final Class<? extends IssueSearcher<SearchableField>> searcherClass;
    private final ComponentFactory factory;
    private final ClauseInformation clauseInformation;
    private final ClauseQueryFactory queryFactory;
    private final ClauseContextFactory clauseContextFactory;
    private final ClauseValidator clauseValidator;
    private final FieldClausePermissionChecker.Factory clausePermissionFactory;
    private final ClauseSanitiser sanitiser;
    private final ClauseValuesGenerator clauseValuesGenerator;

    /**
     * Creates a new factory.
     *
     * @param factory the object that will be used to create the searcher.
     * @param information the string information (clause names, index field, field id) associated with this clause handler.
     * @param searcherClass the class of the searcher to create.
     * @param queryFactory the query factory place in the handler.
     * @param clauseValidator the validatr to place in the handler.
     * @param clausePermissionFactory used create the ClausePermissionHandler. We use this mainly because of a circular
     * @param clauseContextFactory the factory to place in the handler.
     * @param clauseValuesGenerator generates the possible values for a clause.
     */
    public SimpleSearchHandlerFactory(final ComponentFactory factory, final ClauseInformation information,
            final Class<? extends IssueSearcher<SearchableField>> searcherClass,
            final ClauseQueryFactory queryFactory,
            final ClauseValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ClauseContextFactory clauseContextFactory, final ClauseValuesGenerator clauseValuesGenerator)
    {
        this (factory, information, searcherClass, queryFactory, clauseValidator, clausePermissionFactory, clauseContextFactory, clauseValuesGenerator, null);
    }

    /**
     * Creates a new factory.
     *
     * @param factory the object that will be used to create the searcher.
     * @param information contains the string information (clause names, index field, field id) associated with this clause handler.
     * @param searcherClass the class of the searcher to create.
     * @param queryFactory the query factory place in the handler.
     * @param clauseValidator the validatr to place in the handler.
     * @param clausePermissionFactory used create the ClausePermissionHandler. We use this mainly because of a circular
     * @param clauseContextFactory the factory to place in the handler.
     * @param sanitiser the sanitiser to place in the handler. If you want to use {@link com.atlassian.jira.jql.permission.NoOpClauseSanitiser}, use the other constructor.
     */
    public SimpleSearchHandlerFactory(final ComponentFactory factory, final ClauseInformation information,
            final Class<? extends IssueSearcher<SearchableField>> searcherClass,
            final ClauseQueryFactory queryFactory,
            final ClauseValidator clauseValidator, final FieldClausePermissionChecker.Factory clausePermissionFactory,
            final ClauseContextFactory clauseContextFactory, final ClauseValuesGenerator clauseValuesGenerator, ClauseSanitiser sanitiser)
    {
        this.clauseContextFactory = notNull("clauseContextFactory", clauseContextFactory);
        this.clausePermissionFactory = notNull("clausePermissionFactory", clausePermissionFactory);
        this.queryFactory = notNull("queryFactory", queryFactory);
        this.clauseValidator = notNull("clauseValidator", clauseValidator);
        this.clauseInformation = notNull("information", information);
        this.factory = notNull("factory", factory);
        this.searcherClass = notNull("searcherClass", searcherClass);
        this.clauseValuesGenerator = clauseValuesGenerator;
        this.sanitiser = sanitiser;
    }

    public final SearchHandler createHandler(final SearchableField field)
    {
        notNull("field", field);
        final IssueSearcher<SearchableField> searcher = createSearchableField(searcherClass, field);
        final ClauseHandler clauseHandler;
        if (clauseValuesGenerator == null)
        {
            clauseHandler = new DefaultClauseHandler(clauseInformation, queryFactory, clauseValidator, createClausePermissionHandler(field), clauseContextFactory);
        }
        else
        {
            clauseHandler = new DefaultValuesGeneratingClauseHandler(clauseInformation, queryFactory, clauseValidator, createClausePermissionHandler(field), clauseContextFactory, clauseValuesGenerator);
        }
        final SearchHandler.ClauseRegistration registration = new SearchHandler.ClauseRegistration(clauseHandler);

        final SearchHandler.SearcherRegistration searcherRegistration = new SearchHandler.SearcherRegistration(searcher, registration);
        return new SearchHandler(searcher.getSearchInformation().getRelatedIndexers(), searcherRegistration);
    }

    /**
     * Method that creates and initialises a searcher of the passed class.
     *
     * @param clazz the searcher to create.
     * @param field the field the searcher is being created for.
     * @return the new initialised searcher.
     */
    private IssueSearcher<SearchableField> createSearchableField(Class<? extends IssueSearcher<SearchableField>> clazz, final SearchableField field)
    {
        IssueSearcher<SearchableField> searcher = factory.createObject(clazz);
        searcher.init(field);
        return searcher;
    }

    private ClausePermissionHandler createClausePermissionHandler(final Field field)
    {
        if (sanitiser == null)
        {
            return new DefaultClausePermissionHandler(clausePermissionFactory.createPermissionChecker(field));
        }
        else
        {
            return new DefaultClausePermissionHandler(clausePermissionFactory.createPermissionChecker(field), sanitiser);
        }
    }
}
