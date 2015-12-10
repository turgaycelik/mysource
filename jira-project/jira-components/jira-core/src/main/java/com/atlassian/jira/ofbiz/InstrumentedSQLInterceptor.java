package com.atlassian.jira.ofbiz;

import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.instrumentation.InstrumentationName;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.jdbc.interceptors.connection.ConnectionPoolState;
import org.ofbiz.core.entity.jdbc.interceptors.connection.SQLConnectionInterceptor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.atlassian.jira.instrumentation.Instrumentation.pullGauge;
import static com.atlassian.jira.instrumentation.Instrumentation.pullTimer;

/**
 * A SQL interceptor that stores all SQL call invocations as well as timing information in a thread local cache.
 *
 * @since v4.4
 */
public class InstrumentedSQLInterceptor implements SQLConnectionInterceptor
{
    private OpTimer sqlTimer;
    private OpTimer connectionTimer;

    @Override
    public void onConnectionTaken(Connection connection, ConnectionPoolState connectionPoolState)
    {
        pullGauge(InstrumentationName.DB_CONNECTIONS_BORROWED).incrementAndGet();
        connectionTimer = pullTimer(InstrumentationName.DB_CONNECTIONS);
    }

    @Override
    public void onConnectionReplaced(Connection connection, ConnectionPoolState connectionPoolState)
    {
        connectionTimer.end();
        pullGauge(InstrumentationName.DB_CONNECTIONS_BORROWED).decrementAndGet();
    }

    @Override
    public void beforeExecution(String sql, List<String> params, Statement statement)
    {
        if (isMutatingSQL(sql))
        {
            sqlTimer = pullTimer(InstrumentationName.DB_WRITES);
        }
        else
        {
            sqlTimer = pullTimer(InstrumentationName.DB_READS);
        }
    }

    @Override
    public void afterSuccessfulExecution(String sql, List<String> params, Statement statement, ResultSet resultSet, int i)
    {
        sqlTimer.end();
    }

    @Override
    public void onException(String sql, List<String> params, Statement statement, SQLException e)
    {
        sqlTimer.end();
    }

    private boolean isMutatingSQL(String sql)
    {
        String sqlString = StringUtils.defaultString(sql).trim().toUpperCase();
        return sqlString.startsWith("INSERT") ||
                sqlString.startsWith("UPDATE") ||
                sqlString.startsWith("DELETE");
    }
}
