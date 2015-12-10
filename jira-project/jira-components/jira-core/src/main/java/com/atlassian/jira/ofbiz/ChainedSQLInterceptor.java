package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor;
import org.ofbiz.core.entity.jdbc.interceptors.connection.ConnectionPoolState;
import org.ofbiz.core.entity.jdbc.interceptors.connection.SQLConnectionInterceptor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor} that can chain together multiple
 * SQLInterceptors.
 *
 * It will call them in an enveloping order.
 *
 * <pre>
 *  eg if we add 'sqlI1' and 'sqlI2' then it will call
 *
 * sqlI1.beforeExecution(..)
 * sqlI2.beforeExecution(..)
 *
 * sqlI2.afterSuccessfulExecution(..)
 * sqlI1.afterSuccessfulExecution(..)
 * </pre>
 *
 * @since v4.0
 */
public class ChainedSQLInterceptor implements SQLConnectionInterceptor
{
    private final List<SQLConnectionInterceptor> interceptorsList;
    private final List<SQLConnectionInterceptor> reverseInterceptorsList;

    public static class Builder
    {
        private List<SQLConnectionInterceptor> interceptorsList = new ArrayList<SQLConnectionInterceptor>();

        public Builder add(SQLConnectionInterceptor sqlInterceptor)
        {
            interceptorsList.add(sqlInterceptor);
            return this;
        }

        public ChainedSQLInterceptor build()
        {
            return new ChainedSQLInterceptor(interceptorsList);
        }
    }

    public ChainedSQLInterceptor(final List<SQLConnectionInterceptor> interceptorsList)
    {
        this.interceptorsList = new ArrayList<SQLConnectionInterceptor>(interceptorsList);
        this.reverseInterceptorsList = new ArrayList<SQLConnectionInterceptor>(interceptorsList);
        Collections.reverse(reverseInterceptorsList);
    }

    @Override
    public void onConnectionTaken(Connection connection, ConnectionPoolState connectionPoolState)
    {
        for (SQLConnectionInterceptor sqlInterceptor : interceptorsList)
        {
            sqlInterceptor.onConnectionTaken(connection,connectionPoolState);
        }
    }

    @Override
    public void onConnectionReplaced(Connection connection, ConnectionPoolState connectionPoolState)
    {
        for (SQLConnectionInterceptor sqlInterceptor : reverseInterceptorsList)
        {
            sqlInterceptor.onConnectionReplaced(connection,connectionPoolState);
        }
    }

    public void beforeExecution(final String sqlString, final List<String> parameterValues, final Statement statement)
    {
        for (SQLInterceptor sqlInterceptor : interceptorsList)
        {
            sqlInterceptor.beforeExecution(sqlString, parameterValues, statement);
        }
    }

    public void afterSuccessfulExecution(final String sqlString, final List<String> parameterValues, final Statement statement, final ResultSet resultSet, final int rowsUpdated)
    {
        for (SQLInterceptor sqlInterceptor : reverseInterceptorsList)
        {
            sqlInterceptor.afterSuccessfulExecution(sqlString, parameterValues, statement, resultSet,rowsUpdated);
        }
    }

    public void onException(final String sqlString, final List<String> parameterValues, final Statement statement, final SQLException sqlException)
    {
        for (SQLInterceptor sqlInterceptor : reverseInterceptorsList)
        {
            sqlInterceptor.onException(sqlString, parameterValues, statement, sqlException);
        }
    }
}
