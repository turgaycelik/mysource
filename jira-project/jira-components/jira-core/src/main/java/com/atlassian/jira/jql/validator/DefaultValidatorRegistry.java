package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public final class DefaultValidatorRegistry implements ValidatorRegistry
{
    private final SearchHandlerManager manager;
    private final List<ClauseValidator> wasClauseValidators= new ArrayList<ClauseValidator>();
    private final ChangedClauseValidator changedClauseValidator;


    public DefaultValidatorRegistry(final SearchHandlerManager manager, WasClauseValidator wasClauseValidator, ChangedClauseValidator changedClauseValidator)
    {
        this.manager = notNull("manager", manager);
        this.wasClauseValidators.add(notNull("wasClauseValidator", wasClauseValidator));
        this.changedClauseValidator = (notNull("changedClauseValidator", changedClauseValidator));

    }

    public Collection<ClauseValidator> getClauseValidator(final User searcher, final TerminalClause clause)
    {
        notNull("clause", clause);

        Collection<ClauseHandler> clauseHandlers = manager.getClauseHandler(searcher, clause.getName());

        // Collect the factories.
        // JRA-23141 : We avoid using a lazy transformed collection here because it gets accessed multiple times
        // and size() in particular is slow.
        List<ClauseValidator> clauseValidators = new ArrayList<ClauseValidator>(clauseHandlers.size());
        for (ClauseHandler clauseHandler : clauseHandlers)
        {
            clauseValidators.add(clauseHandler.getValidator());
        }
        return clauseValidators;
    }

    @Override
    public Collection<ClauseValidator> getClauseValidator(User searcher, WasClause clause)
    {
        return Collections.unmodifiableCollection(wasClauseValidators);
    }

    @Override
    public ChangedClauseValidator getClauseValidator(User searcher, ChangedClause clause)
    {
        return changedClauseValidator;
    }
}
