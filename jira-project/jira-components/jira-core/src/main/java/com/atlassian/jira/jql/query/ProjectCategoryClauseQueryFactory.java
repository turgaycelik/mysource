package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates queries for the Project Category clause. Since category is not indexed, this factory generates queries on the
 * project field by expanding the category into its projects.
 *
 * @since v4.0
 */
@InjectableComponent
public class ProjectCategoryClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(ProjectCategoryClauseQueryFactory.class);

    private final ProjectCategoryResolver projectCategoryResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public ProjectCategoryClauseQueryFactory(final ProjectCategoryResolver projectCategoryResolver,
            final JqlOperandResolver jqlOperandResolver)
    {
        this.projectCategoryResolver = notNull("projectCategoryResolver", projectCategoryResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!handlesOperator(operator))
        {
            log.debug(String.format("Operator '%s' is not supported.", operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }

        final Set<String> projectIds = getProjectIdsFromClause(queryCreationContext, terminalClause);
        return new QueryFactoryResult(createQueryForValues(projectIds), isNegationOperator(operator));
    }

    private Set<String> getProjectIdsFromClause(final QueryCreationContext queryCreationContext, final TerminalClause clause)
    {
        final List<QueryLiteral> list = jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        final Set<QueryLiteral> rawValues = list != null ? new LinkedHashSet<QueryLiteral>(list) : new LinkedHashSet<QueryLiteral>();
        // if we are negating, we need to exclude projects with no category
        // e.g. category NOT IN ("cat1") is equivalent to category NOT IN ("cat1", EMPTY)
        if (isNegationOperator(clause.getOperator()))
        {
            rawValues.add(new QueryLiteral());
        }

        final Set<String> idStrings = new LinkedHashSet<String>();
        for (QueryLiteral rawValue : rawValues)
        {
            final Collection<Project> projects = projectCategoryResolver.getProjectsForCategory(rawValue);
            for (Project project : projects)
            {
                idStrings.add(project.getId().toString());
            }
        }

        return idStrings;
    }

    private Query createQueryForValues(final Set<String> values)
    {
        if (values.size() == 1)
        {
            final String value = values.iterator().next();
            return getTermQuery(value);
        }
        else
        {
            BooleanQuery combined = new BooleanQuery();
            for (String value : values)
            {
                combined.add(getTermQuery(value), BooleanClause.Occur.SHOULD);
            }
            return combined;
        }
    }

    private TermQuery getTermQuery(final String value)
    {
        return new TermQuery(new Term(SystemSearchConstants.forProjectCategory().getIndexField(), value));
    }

    private boolean isNegationOperator(final Operator operator)
    {
        return operator == Operator.IS_NOT || operator == Operator.NOT_EQUALS || operator == Operator.NOT_IN;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }
}
