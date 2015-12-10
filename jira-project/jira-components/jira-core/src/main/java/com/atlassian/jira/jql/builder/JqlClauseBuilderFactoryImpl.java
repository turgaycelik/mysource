package com.atlassian.jira.jql.builder;

import com.atlassian.jira.jql.util.JqlDateSupport;

public class JqlClauseBuilderFactoryImpl implements JqlClauseBuilderFactory
{
    private final JqlDateSupport dateSupport;

    public JqlClauseBuilderFactoryImpl(JqlDateSupport dateSupport)
    {
        this.dateSupport = dateSupport;
    }

    @Override
    public JqlClauseBuilder newJqlClauseBuilder(JqlQueryBuilder parent)
    {
        return new DefaultJqlClauseBuilder(parent, new PrecedenceSimpleClauseBuilder(), dateSupport);
    }
}
