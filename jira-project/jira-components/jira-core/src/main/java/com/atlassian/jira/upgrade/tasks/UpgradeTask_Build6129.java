package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseType;
import org.ofbiz.core.entity.jdbc.dbtype.DatabaseTypeFactory;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Extract Issue number to a separate column
 *
 * @since v6.1
 */
public class UpgradeTask_Build6129 extends AbstractUpgradeTask
{
    public static final String ISSUE_ENTITY_NAME = "Issue";
    public static final String ISSUE_TABLE_NAME = "jiraissue";

    private static final Logger log = Logger.getLogger(UpgradeTask_Build6129.class);

    public UpgradeTask_Build6129()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6129";
    }

    @Override
    public String getShortDescription()
    {
        return "Extract Issue number to a separate column";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final DateTime startedAt = new DateTime();
        final Connection connection = getDatabaseConnection();
        try
        {
            connection.setAutoCommit(false);

            if (isPostgreSQL())
            {
                postgresUpgrade(connection);
            }
            else if (isMYSQL())
            {
                mysqlUpgrade(connection);
            }
            else if (isORACLE())
            {
                oracleUpgrade(connection);
            }
            else if (isMSSQL())
            {
                msSqlUpgrade(connection);
            }

            // Run the genericUpgrade regardless.  If the issues are already handled by the db-specific
            // versions or by IssueGenericEntity during the import, then it'll just fetch 0 rows and be
            // done with it.
            genericUpgrade(connection);
        }
        finally
        {
            log.info(String.format("Upgrade task took %d seconds to complete", Seconds.secondsBetween(startedAt, new DateTime()).getSeconds()));
            connection.close();
        }
    }

    private void msSqlUpgrade(final Connection connection) throws SQLException, GenericEntityException
    {
        substrUpgrade(connection, new MsSqlSubstringBuilder(), "NUMERIC");
    }

    private void oracleUpgrade(final Connection connection) throws SQLException, GenericEntityException
    {
        substrUpgrade(connection, new NormalSqlSubstringBuilder(), "NUMBER");
    }

    private Map<String,Long> getAllProjects()
    {
        return Select.columns("id", "key")
                .from("Project")
                .runWith(getEntityEngine())
                .consumeWith(new ProjectConsumer());
    }

    private void postgresUpgrade(final Connection connection) throws SQLException, GenericEntityException
    {
        substrUpgrade(connection, new NormalSqlSubstringBuilder(), "INT");
    }

    private void mysqlUpgrade(final Connection connection) throws SQLException, GenericEntityException
    {
        substrUpgrade(connection, new NormalSqlSubstringBuilder(), "UNSIGNED");
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"}, justification="Non-constant but safe.")
    private void substrUpgrade(final Connection connection, final SqlSubstringBuilder substr, final String castType)
            throws SQLException, GenericEntityException
    {
        final ModelEntity issueTable = getOfBizDelegator().getModelReader().getModelEntity(ISSUE_ENTITY_NAME);
        final String projectColumn = issueTable.getField("project").getColName();
        final String issueNumColumn = issueTable.getField("number").getColName();
        final String keyColumn = issueTable.getField("key").getColName();

        Statement statement = connection.createStatement();
        for (Map.Entry<String,Long> entry : getAllProjects().entrySet())
        {
            final String projectKey = entry.getKey();
            final Long projectId = entry.getValue();

            try
            {
                // SQL strings start at 1, and we need to skip the '-', so... +2
                final int index = projectKey.length() + 2;

                final String update = "UPDATE " + convertToSchemaTableName(ISSUE_TABLE_NAME)
                    + " SET " + issueNumColumn + "=CAST(" + substr.of(keyColumn, index) + " AS " + castType + "), " + keyColumn + "=NULL"
                    + " WHERE " + projectColumn + '=' + projectId
                    + " AND " + keyColumn + " IS NOT NULL";
                final int updated = statement.executeUpdate(update);
                connection.commit();
                if (updated > 0)
                {
                    log.info("[" + projectKey + "] issues updated: " + updated);
                }
            }
            catch (SQLException sqle)
            {
                log.warn("[" + projectKey + "] fast upgrade failed (will fall back on slow upgrade): " + sqle);
                connection.rollback();
                // Do not propagate the exception; let the genericUpgrade have a crack at it...
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification="Non-constant but safe.")
    private void genericUpgrade(final Connection connection) throws Exception
    {
        try
        {
            final long issuesInTotal = getIssueCount();
            log.info("Total issue count: " + issuesInTotal);

            final ModelEntity issueTable = getOfBizDelegator().getModelReader().getModelEntity(ISSUE_ENTITY_NAME);
            final String issueNumColumn = issueTable.getField("number").getColName();
            final String keyColumn = issueTable.getField("key").getColName();
            final String idColumn = issueTable.getField("id").getColName();

            final String selectSql = "SELECT " + idColumn +", " + keyColumn + " FROM " + convertToSchemaTableName(ISSUE_TABLE_NAME) + " WHERE " + keyColumn + " IS NOT NULL ";
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
            try
            {
                final String updateSql = "UPDATE " + convertToSchemaTableName(ISSUE_TABLE_NAME) + " SET " + issueNumColumn + "= ? WHERE " + idColumn + "= ? ";
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                try
                {
                    ResultSet rs = selectStmt.executeQuery();
                    try
                    {
                        genericUpgradeBatch(connection, issuesInTotal, updateStmt, rs);
                    }
                    finally
                    {
                        rs.close();
                    }
                    updateStmt.executeBatch();
                }
                finally
                {
                    updateStmt.close();
                }
            }
            finally
            {
                selectStmt.close();
            }
            connection.commit();
        }
        catch (Exception e)
        {
            connection.rollback();
            throw e;
        }
    }

    private void genericUpgradeBatch(final Connection connection, final long issuesInTotal,
            final PreparedStatement updateStmt, final ResultSet rs) throws SQLException
    {
        DateTime lastReport = new DateTime();
        long processed = 0;
        while (rs.next())
        {
            String pkey = null;
            long issueNum = -1L;
            long id = -1L;

            try
            {
                pkey = rs.getString("pkey");
                id = rs.getLong("id");
                issueNum = getIssueNumberFromKey(pkey);

                updateStmt.setLong(1, issueNum);
                updateStmt.setLong(2, id);
                updateStmt.addBatch();

                processed++;
                if (processed % 250 == 0)
                {
                    updateStmt.executeBatch();
                    connection.commit();
                }

                final DateTime now = new DateTime();
                if (Seconds.secondsBetween(lastReport, now).isGreaterThan(Seconds.seconds(30)))
                {
                    lastReport = now;
                    log.info(String.format("Processed %d Issues, %d left for processing", processed, issuesInTotal - processed));
                }
            }
            catch (SQLException sqle)
            {
                log.error("Update failed in batch.  Detected at processed=" + processed + "; pkey=" + pkey + "; issueNum=" + issueNum + "; id=" + id + "; sqle=" + sqle);

                SQLException ex = sqle.getNextException();
                while (ex != null)
                {
                    log.error("Chained exception: " + ex);
                    ex = ex.getNextException();
                }

                throw sqle;
            }
        }
        log.info("Total issues handled row-at-a-time: " + processed);
    }



    protected long getIssueCount()
    {
        return Select.countFrom(ISSUE_ENTITY_NAME).runWith(getOfBizDelegator()).singleValue();
    }

    protected long getIssueNumberFromKey(final String pkey)
    {
        return IssueKey.from(pkey).getIssueNumber();
    }

    static interface SqlSubstringBuilder
    {
        String of(String column, int index);
    }

    static class NormalSqlSubstringBuilder implements SqlSubstringBuilder
    {
        public String of(final String column, final int index)
        {
            return "SUBSTR(" + column + ',' + index + ')';
        }
    }

    static class MsSqlSubstringBuilder implements SqlSubstringBuilder
    {
        public String of(final String column, final int index)
        {
            return "SUBSTRING(" + column + ',' + index + ",999)";
        }
    }



    static class ProjectConsumer implements EntityListConsumer<GenericValue,Map<String,Long>>
    {
        final Map<String,Long> result = new TreeMap<String,Long>();

        @Override
        public void consume(final GenericValue gv)
        {
            result.put(gv.getString("key"), gv.getLong("id"));
        }

        @Override
        public Map<String, Long> result()
        {
            return result;
        }
    }
}

