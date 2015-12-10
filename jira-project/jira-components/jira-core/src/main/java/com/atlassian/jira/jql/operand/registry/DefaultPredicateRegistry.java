package com.atlassian.jira.jql.operand.registry;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.operator.Operator;

import java.util.Collection;


/**
 *  For now only the BY predicate supports searching   , the FROM and TO predicates also support seaarching
 *
 * @since v4.4
 */
public class DefaultPredicateRegistry implements PredicateRegistry
{
    private final UserPickerSearchService userPickerSearchService;
    private final SearchHandlerManager searchHandlerManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultPredicateRegistry(UserPickerSearchService userPickerSearchService, SearchHandlerManager searchHandlerManager, JiraAuthenticationContext authenticationContext)
    {
        this.userPickerSearchService = userPickerSearchService;
        this.searchHandlerManager = searchHandlerManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public ClauseValuesGenerator getClauseValuesGenerator(final String predicateName, final String fieldName)
    {
        if (Operator.BY.name().equalsIgnoreCase(predicateName))
        {
            return new UserClauseValuesGenerator(userPickerSearchService);
        }
        if  (Operator.FROM.name().equalsIgnoreCase(predicateName) || Operator.TO.name().equalsIgnoreCase(predicateName) )
        {
            return getClauseValuesGeneratorForField(fieldName);
        }
        return null;
    }

    private ClauseValuesGenerator getClauseValuesGeneratorForField(final String fieldName)
    {
        final User searcher = authenticationContext.getLoggedInUser();
        final Collection<ClauseHandler> clauseHandlers = searchHandlerManager.getClauseHandler(searcher, fieldName);
        if (clauseHandlers != null && clauseHandlers.size() == 1)
        {
            ClauseHandler clauseHandler = clauseHandlers.iterator().next();

            if (clauseHandler instanceof ValueGeneratingClauseHandler)
            {
                return ((ValueGeneratingClauseHandler) clauseHandler).getClauseValuesGenerator();
            }
        }
        return null;
    }
}
