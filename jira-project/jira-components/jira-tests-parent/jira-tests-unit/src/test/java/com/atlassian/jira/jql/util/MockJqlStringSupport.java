package com.atlassian.jira.jql.util;

import java.util.Set;

import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

/**
 * Simple NO-OP mock of the {@link com.atlassian.jira.jql.util.JqlStringSupport} interface. 
 *
 * @since v4.0
 */
public class MockJqlStringSupport implements JqlStringSupport
{
    public String encodeStringValue(final String value)
    {
        return value;
    }

    public String encodeValue(final String value)
    {
        return value;
    }

    public String encodeFunctionArgument(final String argument)
    {
        return argument;
    }

    public String encodeFunctionName(final String functionName)
    {
        return functionName;
    }

    public String encodeFieldName(final String fieldName)
    {
        return fieldName;
    }

    public String generateJqlString(final Query query)
    {
        final ToJqlStringVisitor jqlStringVisitor = new ToJqlStringVisitor(this);
        return jqlStringVisitor.toJqlString(query.getWhereClause());
    }

    public String generateJqlString(final Clause clause)
    {
        final ToJqlStringVisitor jqlStringVisitor = new ToJqlStringVisitor(this);
        return jqlStringVisitor.toJqlString(clause);
    }

    public Set<String> getJqlReservedWords()
    {
        return null;
    }
}
