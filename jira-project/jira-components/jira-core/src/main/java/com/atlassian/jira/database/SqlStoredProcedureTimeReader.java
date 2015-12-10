package com.atlassian.jira.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;

public class SqlStoredProcedureTimeReader implements DatabaseSystemTimeReader
{
    private final OfBizConnectionFactory connectionFactory;
    private final String code;

    public SqlStoredProcedureTimeReader(@Nonnull String code)
    {
        this.connectionFactory = DefaultOfBizConnectionFactory.getInstance();
        this.code = code;
    }

    @Override
    public long getDatabaseSystemTimeMillis() throws SQLException
    {
        Connection con = connectionFactory.getConnection();
        try
        {
            CallableStatement stat = con.prepareCall(code);
            try
            {
                stat.registerOutParameter(1, Types.NUMERIC);
                stat.execute();
                return(stat.getLong(1));
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
