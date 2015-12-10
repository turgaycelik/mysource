package com.atlassian.jira.database;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;

import org.ofbiz.core.entity.jdbc.dbtype.AbstractPostgresDatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.DB2DatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.HsqlDatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.MsSqlDatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.MySqlDatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.Oracle10GDatabaseType;

/**
 * Default database system time reader factory implementation that creates an appropriate database system time reader
 * based on the database type read from the currently active connection factory.
 *
 * @since 6.3.4
 */
public class DefaultDatabaseSystemTimeReaderFactory implements DatabaseSystemTimeReaderFactory
{
    private final OfBizConnectionFactory connectionFactory;

    public DefaultDatabaseSystemTimeReaderFactory()
    {
        this.connectionFactory = DefaultOfBizConnectionFactory.getInstance();
    }

    @Nonnull
    @Override
    public DatabaseSystemTimeReader getReader()
    {
        DatabaseType dbType = connectionFactory.getDatasourceInfo().getDatabaseTypeFromJDBCConnection();

        if (dbType instanceof AbstractPostgresDatabaseType)
        {
            return new SqlStatementTimeReader("values (current_timestamp)");
        }
        else if (dbType instanceof MySqlDatabaseType || dbType instanceof MsSqlDatabaseType)
        {
            return new SqlStatementTimeReader("select current_timestamp");
        }
        else if (dbType instanceof Oracle10GDatabaseType || "oracle".equals(dbType.getFieldTypeName()))
        {
            return new SqlStatementTimeReader("select systimestamp from dual");
        }
        else if (dbType instanceof HsqlDatabaseType)
        {
            //Even though this is a CALL, result must still be retrierved through a prepared statement, not callable statement
            return new SqlStatementTimeReader("CALL current_timestamp");
        }
        else if (dbType instanceof DB2DatabaseType)
        {
            return new SqlStatementTimeReader("SELECT CURRENT TIMESTAMP FROM sysibm.sysdummy1");
        }
        else
        {
            throw new RuntimeException("Database type '" + dbType.getName() + "' is not supported for retrieving database system times.");
        }
    }
}
