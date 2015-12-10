package com.atlassian.jira.issue.search.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestRedundantClausesQueryOptimizer
{
    private QueryOptimizer optimizer;

    @Before
    public void setUp() throws Exception
    {
        optimizer = new RedundantClausesQueryOptimizer();
    }

    @Test
    public void testRedundantClausesQueryOptimizer() throws Exception
    {
        JqlQueryParser parser = new DefaultJqlQueryParser();

        List<String> jqlStringUnoptimized = new ArrayList<String>();
        List<String> jqlStringOptimized = new ArrayList<String>();

        jqlStringUnoptimized.add("(status = \"Open\" and resolution = Unresolved and version = 123 and project = Project1) or"
                + " (project = Project1 and project = Project1 and project = Project1 and status = \"Open\")");
        jqlStringOptimized.add("(status = \"Open\" and resolution = Unresolved and version = 123 and project = Project1) or (project = Project1 and status = \"Open\")");

        jqlStringUnoptimized.add("project = ABC AND version = 123 OR project = DEF and version = 567 and version = 567 and status = Open");
        jqlStringOptimized.add("project = ABC AND version = 123 OR project = DEF and version = 567 and status = Open");

        jqlStringUnoptimized.add("assignee is empty AND description ~ \"hello\" AND reporter IS EMPTY AND project = ABC AND version = 123 AND project = ABC AND version = 123");
        jqlStringOptimized.add("assignee is empty AND description ~ \"hello\" AND reporter IS EMPTY AND project = ABC AND version = 123");

        jqlStringUnoptimized.add("assignee is empty AND description ~ \"hello\" AND reporter IS EMPTY AND project = ABC AND version = 123 AND project = ABC AND version = 123");
        jqlStringOptimized.add("assignee is empty AND description ~ \"hello\" AND reporter IS EMPTY AND project = ABC AND version = 123");

        int i = 0;
        for (Iterator<String> iterator = jqlStringUnoptimized.iterator(); iterator.hasNext();)
        {
            String jqlString =  iterator.next();
            final Query query = parser.parseQuery(jqlString);
            final Query optimizedQuery = optimizer.optimizeQuery(query);
            final Query expectedOptimizedQuery = parser.parseQuery(jqlStringOptimized.get(i++));
            assertEquals(new QueryImpl(expectedOptimizedQuery.getWhereClause()), optimizedQuery);
        }

        TerminalClause t1 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");
        TerminalClause t2 = new TerminalClauseImpl("status", Operator.EQUALS, "open");
        TerminalClause t3 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");

        Query query = new QueryImpl(new AndClause(new AndClause(t1, t2), new AndClause(t3)));
        final Query optimizedQuery = optimizer.optimizeQuery(query);
        final Query expectedQuery = new QueryImpl(new AndClause(t1, t2));
        assertEquals(expectedQuery, optimizedQuery);

        Query queryWithSort = new QueryImpl(new AndClause(new AndClause(t1, t2), new AndClause(t3)), new OrderByImpl(new SearchSort("project", SortOrder.ASC)), "");
        final Query optimizedQueryWithSort = optimizer.optimizeQuery(queryWithSort);
        final Query expectedQueryWithSort = new QueryImpl(new AndClause(t1, t2), new OrderByImpl(new SearchSort("project", SortOrder.ASC)), null);
        assertEquals(expectedQueryWithSort, optimizedQueryWithSort);

        TerminalClause t4 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");
        TerminalClause t5 = new TerminalClauseImpl("status", Operator.EQUALS, "open");
        TerminalClause t6 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");

        Query queryWithOr = new QueryImpl(new OrClause(new OrClause(t4, t5), new OrClause(t6)));
        final Query optimizedQueryWithOr = optimizer.optimizeQuery(queryWithOr);
        final Query expectedQueryWithOr = new QueryImpl(new OrClause(new OrClause(t4, t5), new OrClause(t6)));
        assertEquals(expectedQueryWithOr, optimizedQueryWithOr);
    }

    @Test
    public void testRedundantClausesQueryOptimizerNotClauses() throws Exception
    {
        TerminalClause t1 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");
        TerminalClause t2 = new TerminalClauseImpl("status", Operator.EQUALS, "open");
        TerminalClause t3 = new TerminalClauseImpl("project", Operator.EQUALS, "ABC");
        OrClause t4 = new OrClause(new AndClause(new TerminalClauseImpl("project", Operator.EQUALS, "ABC"), new TerminalClauseImpl("component", Operator.EQUALS, "My Component"), new TerminalClauseImpl("project", Operator.EQUALS, "ABC") ));

        Query query = new QueryImpl(new NotClause(new AndClause(new AndClause(t1, t2, t4), new AndClause(t3))));
        final Query optimizedQuery = optimizer.optimizeQuery(query);
        final Query expectedQuery = new QueryImpl(new NotClause(new AndClause(t1, t2, new OrClause(new AndClause(new TerminalClauseImpl("project", Operator.EQUALS, "ABC"), new TerminalClauseImpl("component", Operator.EQUALS, "My Component") )))));
        assertEquals(expectedQuery, optimizedQuery);
    }



}
