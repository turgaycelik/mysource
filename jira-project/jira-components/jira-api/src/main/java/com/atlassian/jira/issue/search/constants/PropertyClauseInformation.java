package com.atlassian.jira.issue.search.constants;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Sets;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Entity property clause information.
 * @since 6.2
 */
public class PropertyClauseInformation implements ClauseInformation
{
    public static final Set<Operator> operators = Sets.union(OperatorClasses.EQUALITY_AND_RELATIONAL, OperatorClasses.TEXT_OPERATORS);
    private static final JiraDataType dataType = new JiraDataTypeImpl(newHashSet(String.class, Number.class, Date.class));

    private final ClauseNames clauseNames;

    public PropertyClauseInformation(final ClauseNames clauseNames)
    {
        this.clauseNames = clauseNames;
    }


    @Override
    public ClauseNames getJqlClauseNames()
    {
        return clauseNames;
    }

    @Nullable
    @Override
    public String getIndexField()
    {
        return null;
    }

    @Nullable
    @Override
    public String getFieldId()
    {
        return null;
    }

    @Override
    public Set<Operator> getSupportedOperators()
    {
        return operators;
    }

    @Override
    public JiraDataType getDataType()
    {
        return dataType;
    }
}
