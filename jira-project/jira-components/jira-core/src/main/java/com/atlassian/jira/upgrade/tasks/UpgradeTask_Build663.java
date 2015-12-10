package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Fix any possible corruption in the jiraissue table due to https://jira.atlassian.com/browse/JRA-25914  , also reindexes
 *
 * @since v4.4.3
 */
public class UpgradeTask_Build663 extends AbstractReindexUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build663.class);

    public UpgradeTask_Build663()
    {
        super();
    }


    @Override
    public String getBuildNumber()
    {
        return "663";
    }

    @Override
    public String getShortDescription()
    {
        return "Detect and repair duplicate keys in JIRA table";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();

        boolean committed = false;
        try
        {
            connection.setAutoCommit(false);
            final Map<String, Set<String>> duplicateKeys = checkDuplicatesExist(connection,"jiraissue");
            if (duplicateKeys.size() > 0)
            {
                patchIssues(connection, duplicateKeys);
            }
            connection.commit();
            committed = true;
            if (duplicateKeys.size() > 0)
            {
                super.doUpgrade(setupMode);
            }
        }
        finally
        {
            if (!committed)
            {
                connection.rollback();
            }
            connection.close();
        }
    }

    private Map<String, Set<String>> checkDuplicatesExist(Connection connection, String tableName)  throws SQLException
    {
        final Map<String, Set<String>> duplicateKeys = Maps.newHashMap();
        String sql = "select pkey from " + convertToSchemaTableName(tableName) + " group by pkey having count(pkey) > 1";
        Statement select = connection.createStatement();
        ResultSet rs = select.executeQuery(sql);
        while (rs.next()) {
            final String issueKey = rs.getString(1);
            final String projectKey = extractProjectKey(issueKey);
            if (duplicateKeys.containsKey(projectKey))
            {
                duplicateKeys.get(projectKey).add(issueKey);
            }
            else
            {
                Set<String> issueKeys = Sets.newHashSet();
                issueKeys.add(issueKey);
                duplicateKeys.put(projectKey, issueKeys);
            }
        }
        rs.close();
        select.close();
        return duplicateKeys;
    }


    private void patchIssues(Connection connection, Map<String, Set<String>>duplicateKeys) throws SQLException
    {
        String jiraIssueTable = convertToSchemaTableName("jiraissue");
        String selectSql  = "select pcounter from " + convertToSchemaTableName("project")+ " where pkey=?";
        String selectSql2 =  "select pkey from " + jiraIssueTable + " where id=(select max(id) from " + jiraIssueTable +" where pkey like ?)";
        String selectSql3 = "select id from "   + jiraIssueTable + " where pkey=?";
        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        PreparedStatement selectStmt2 = connection.prepareStatement(selectSql2);
        PreparedStatement selectStmt3 = connection.prepareStatement(selectSql3);

        for (String pKey : duplicateKeys.keySet())
        {
            int maxCountFromProjectTable = 0;
            int maxCountFromIssueTable = 0;
            int safeProjectCounter = 0;
            selectStmt.setString(1, pKey);
            selectStmt2.setString(1, makeLikePatternForKey(pKey));
            ResultSet projectCounterResultSet = selectStmt.executeQuery();
            if (projectCounterResultSet.next())
            {
                maxCountFromProjectTable = projectCounterResultSet.getInt(1);
            }
            ResultSet issueKeyCounterResultSet = selectStmt2.executeQuery();
            if (issueKeyCounterResultSet.next())
            {
                maxCountFromIssueTable = parseIssueKey(issueKeyCounterResultSet.getString(1));
            }
            safeProjectCounter = (maxCountFromProjectTable > maxCountFromIssueTable) ? maxCountFromProjectTable :  maxCountFromIssueTable;
            for (String issueKey : duplicateKeys.get(pKey) )
            {
                final Set <Long> ids= Sets.newHashSet();
                selectStmt3.setString(1, issueKey);
                ResultSet idsResultSet = selectStmt3.executeQuery();
                while (idsResultSet.next())
                {
                    ids.add(idsResultSet.getLong(1));
                }
                safeProjectCounter = fixDuplicates(connection, pKey, ids, safeProjectCounter);
            }
            updateProjectCounter(connection, pKey, safeProjectCounter);
        }
        selectStmt.close();
        selectStmt2.close();
        selectStmt3.close();
    }

    private void updateProjectCounter(Connection connection, String pKey, int safeProjectCounter) throws SQLException
    {
        String updateSql  = "update " + convertToSchemaTableName("project") + " set pcounter = ? where pkey = ? ";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setInt(1, safeProjectCounter);
        updateStmt.setString(2, pKey);
        updateStmt.executeUpdate();
        updateStmt.close();
    }

    private int fixDuplicates(Connection connection, String pKey, Set<Long> ids, int safeProjectCounter) throws SQLException
    {
        String updateSql  = "update " + convertToSchemaTableName("jiraissue") + " set pkey = ? where id = ? ";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        Iterator<Long> iter = ids.iterator();
        iter.next();
        while (iter.hasNext())
        {
            safeProjectCounter++;
            final String issueKey = pKey+"-"+safeProjectCounter;
            final Long id = iter.next();
            updateStmt.setString(1, issueKey);
            updateStmt.setLong(2, id);
            updateStmt.executeUpdate();
            log.info(String.format("Found issue id %d with duplicate key: Replacing with %s", id, issueKey));
            updateStmt.close();
        }
        return safeProjectCounter;
    }

    private int parseIssueKey(String pKey)
    {
        final int index = pKey.indexOf("-");
        if (index >= 0)
        {
            return Integer.parseInt(pKey.substring(index + 1));
        }
        return 0;
    }

    private String makeLikePatternForKey(String pKey)
    {
        return extractProjectKey(pKey) + "-%";
    }

    private String extractProjectKey(String pKey)
    {
        final int index = pKey.indexOf("-");
        if (index >= 0)
        {
            return pKey.substring(0, index);
        }
        return pKey;
    }
}
