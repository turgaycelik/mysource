package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.MultiClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Visitor that looks at a clause and determines the searchers that are relevant.
 * For each {@link com.atlassian.query.clause.TerminalClause} it is determined what {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}
 * is responsible for its searching.
 *
 * The searching user is passed into the constructor for visibility checking.
 *
 * @since v4.0
 */
public class RelevantSearcherVisitor implements ClauseVisitor<Boolean>
{
    private static final Logger log = Logger.getLogger(RelevantSearcherVisitor.class);

    private final Map<String, IssueSearcher> searchers = new HashMap<String, IssueSearcher>();
    private final User user;
    private final SearchHandlerManager searchHandlerManager;

    public RelevantSearcherVisitor(final SearchHandlerManager searchHandlerManager, final User user)
    {
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
        this.user = user;
    }

    public Set<IssueSearcher> getRelevantSearchers()
    {
        return new HashSet<IssueSearcher>(searchers.values());
    }

    public Boolean visit(final AndClause andClause)
    {
        return visitMultiClause(andClause);
    }

    public Boolean visit(final NotClause notClause)
    {
        return notClause.getSubClause().accept(this);
    }

    public Boolean visit(final OrClause orClause)
    {
        return visitMultiClause(orClause);
    }

    public Boolean visit(final TerminalClause clause)
    {
        final IssueSearcher searcher = getSearcher(clause);
        if (searcher == null)
        {
            return false;
        }
        searchers.put(searcher.getSearchInformation().getId(), searcher);
        return true;
    }

    @Override
    public Boolean visit(WasClause clause)
    {
        // History searches do not participate in simple view
        // there are no relevant searchers
        return false;
    }

    @Override
    public Boolean visit(ChangedClause clause)
    {
        // Changed searches do not participate in simple view
        // there are no relevant searchers
        return false;
    }


    private boolean visitMultiClause(final MultiClause multiClause)
    {
        boolean returnValue = true;
        for (Iterator<Clause> iterator = multiClause.getClauses().iterator(); returnValue && iterator.hasNext();)
        {
            Clause clause = iterator.next();
            returnValue &= clause.accept(this);
        }
        return returnValue;
    }

    private IssueSearcher getSearcher(final TerminalClause clause)
    {
        final Collection<IssueSearcher<?>> searchersByClauseName = searchHandlerManager.getSearchersByClauseName(user, clause.getName());
        if (searchersByClauseName.size() == 1 )
        {
            return searchersByClauseName.iterator().next();
        }
        log.debug(String.format("Unable to resolve only one searcher for field '%s', found '%d' searchers", clause.getName(), searchersByClauseName.size()));
        return null;
    }
}
