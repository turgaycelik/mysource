package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.query.clause.TerminalClause;

import java.util.ArrayList;
import java.util.List;

/**
 * A query factory that can generate queries for clauses that represent {@link com.atlassian.jira.project.Project}'s.
 *
 * @since v4.0
 */
public class ProjectCustomFieldClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    ///CLOVER:OFF

    public ProjectCustomFieldClauseQueryFactory(String documentConstant, ProjectResolver projectResolver, JqlOperandResolver jqlOperandResolver)
    {
        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(projectResolver);
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new EqualityQueryFactory<Project>(projectIndexInfoResolver));
        delegateClauseQueryFactory = new GenericClauseQueryFactory(documentConstant, operatorFactories, jqlOperandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

    ///CLOVER:ON
}
