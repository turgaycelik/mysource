package com.atlassian.jira.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;

public class SqlStatementTimeReader implements DatabaseSystemTimeReader
{
    private final OfBizConnectionFactory connectionFactory;
    private final String statement;

    public SqlStatementTimeReader(@Nonnull String statement)
    {
        this.connectionFactory = DefaultOfBizConnectionFactory.getInstance();
        this.statement = statement;
    }

    @Override
    public long getDatabaseSystemTimeMillis() throws SQLException
    {
        Connection con = connectionFactory.getConnection();
        try
        {
            PreparedStatement stat = con.prepareStatement(statement);
            try
            {
                ResultSet rs = stat.executeQuery();
                try
                {
                    rs.next();
                    Timestamp now = rs.getTimestamp(1);
                    return(now.getTime());
                }
                finally
                {
                    DatabaseUtil.closeQuietly(rs);
                }
            }
            finally
            {
                DatabaseUtil.closeQuietly(stat);
            }
        }
        finally
        {
            DatabaseUtil.closeQuietly(con);
        }
    }
}
