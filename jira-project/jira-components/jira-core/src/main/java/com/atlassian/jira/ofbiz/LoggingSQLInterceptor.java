package com.atlassian.jira.ofbiz;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.jdbc.interceptors.connection.ConnectionPoolState;
import org.ofbiz.core.entity.jdbc.interceptors.connection.SQLConnectionInterceptor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * A logging implementation of {@link org.ofbiz.core.entity.jdbc.interceptors.SQLInterceptor} that can log SQL as it
 * happens inside JIRA
 *
 * @since v4.0
 */
class LoggingSQLInterceptor implements SQLConnectionInterceptor
{
    private static final Logger log = Logger.getLogger(LoggingSQLInterceptor.class);

    private long startTime;
    private long connectionStartTime;


    @Override
    public void onConnectionTaken(Connection connection, ConnectionPoolState connectionPoolState)
    {
        connectionStartTime = System.currentTimeMillis();
        if (log.isInfoEnabled())
        {
            log.info(String.format("%dms Connection taken. borrowed : %d", connectionPoolState.getTimeToBorrow(), connectionPoolState.getBorrowedCount()));
        }
    }

    @Override
    public void onConnectionReplaced(Connection connection, ConnectionPoolState connectionPoolState)
    {
        if (log.isInfoEnabled())
        {
            long borrowTime = System.currentTimeMillis() - connectionStartTime;
            log.info(String.format("%dms Connection returned. borrowed : %d", borrowTime, connectionPoolState.getBorrowedCount()));
        }
    }

    public void beforeExecution(final String sqlString, final List<String> parameterValues, final Statement statement)
    {
        startTime = System.currentTimeMillis();
    }

    public void afterSuccessfulExecution(final String sqlString, final List<String> parameterValues, final Statement statement, final ResultSet resultSet, final int rowsUpdated)
    {
        afterExecutionImpl(sqlString, parameterValues, statement, null);
    }

    public void onException(final String sqlString, final List<String> parameterValues, final Statement statement, final SQLException sqlException)
    {
        afterExecutionImpl(sqlString, parameterValues, statement, sqlException);
    }

    private void afterExecutionImpl(final String sqlString, final List<String> parameterValues, final Statement statement, final SQLException possibleException)
    {
        if (log.isInfoEnabled())
        {
            long ms = System.currentTimeMillis() - startTime;
            final String msg = ms + "ms " + OfBizLogHelper.formatSQL(sqlString, parameterValues);
            if (possibleException != null)
            {
                log.error(msg, possibleException);
            }
            else
            {
                log.info(msg);
                if (log.isDebugEnabled())
                {
                    log.debug(OfBizLogHelper.logTheCallStack());
                }
            }
        }
    }
}
