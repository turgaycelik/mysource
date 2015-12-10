package com.atlassian.jira.upgrade.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.appconsistency.db.TableColumnCheckResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestUpgradeUtils
{
    private UpgradeUtils upgradeUtils = null;
    private OfBizDelegator ofBizDelegator = null;
    private static final String QRTZJOB_DETAILS = "QRTZJobDetails";
    private static final String JOB_NAME_COL = "JOB_NAME";
    private static final String JOB_NAME = "jobName";
    private static final String QRTZ_JOB_DETAILS_TABLE = "qrtz_job_details";
    private static final String DESCRIPTION_COLUMN_NAME = "JIRA_DESC";

    private static final String PROPERTY_ENTRY_TABLE = "PROPERTYENTRY";
    private static final String PROPERTY_STRING_TABLE = "PROPERTYSTRING";
    private static final String PROPERTY_STRING_PROPERTY_VALUE = "PROPERTYVALUE";
    private static final String PROPERTY_ENTRY_PROPERTY_KEY = "PROPERTY_KEY";
    private static final String DATASOURCE_NAME = "defaultDS";
    private BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();
    private DatasourceInfo datasourceInfo= EntityConfigUtil.getInstance().getDatasourceInfo(DATASOURCE_NAME);

    @Mock
    private DatabaseConfigurationManager databaseConfigurationManager;

    @Mock
    private OfBizConnectionFactory ofBizConnectionFactory;

    @Rule
    public final ClearStatics clearStatics = new ClearStatics();

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(databaseConfigurationManager.getDatabaseConfiguration()).thenReturn(UtilsForTestSetup.getDatabaseConfig());
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(DatabaseConfigurationManager.class, databaseConfigurationManager);

        when(ofBizConnectionFactory.getDatasourceInfo()).thenReturn(datasourceInfo);
        when(ofBizConnectionFactory.getConnection()).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                return ConnectionFactory.getConnection(DATASOURCE_NAME);
            }
        });

        UtilsForTestSetup.loadDatabaseDriver();
        UtilsForTestSetup.deleteAllEntities();

        ofBizDelegator = OfBizFactory.getOfBizDelegator();


        componentAccessorWorker.registerMock(OfBizDelegator.class, ofBizDelegator);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);

        upgradeUtils = new UpgradeUtils(ofBizDelegator);

        // Create a column that is not in the entitymodel.xml
        alterColumnOnTable(true);

        // Setup table to play with
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 1, "value 1");
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 2, "value 2");
        insertIntoTable(QRTZ_JOB_DETAILS_TABLE, DESCRIPTION_COLUMN_NAME, 3, "value 3");

        // Set up some version information for JIRA
        insertIntoTable(PROPERTY_ENTRY_TABLE, PROPERTY_ENTRY_PROPERTY_KEY, 1, "jira.version.patched");
        insertIntoTable(PROPERTY_STRING_TABLE, PROPERTY_STRING_PROPERTY_VALUE, 1, buildUtilsInfo.getCurrentBuildNumber());

    }

    @After
    public void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
        alterColumnOnTable(false);
    }

    @Test
    public void testClearColumns() throws GenericEntityException
    {
        EntityUtils.createValue(QRTZJOB_DETAILS, MapBuilder.<String, Object>build("id", "4", JOB_NAME, "value 1"));

        upgradeUtils.clearColumn(QRTZJOB_DETAILS, JOB_NAME);

        List<GenericValue> jobs = ofBizDelegator.findAll(QRTZJOB_DETAILS);
        for (Iterator<GenericValue> iterator = jobs.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = iterator.next();
            assertNull(genericValue.getString(JOB_NAME));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClearColumnWithInvalidColumn()
    {
        upgradeUtils.clearColumn(QRTZJOB_DETAILS, "junkOne");
    }

    @Test
    public void testGetExactColumnName()
    {
        String tableName_issueType = "issuetype";
        String exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "ISSUETYPE");
        assertNull(exactColumnName);

        exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "PNAME");
        assertTrue("pname".equalsIgnoreCase(exactColumnName));

        tableName_issueType = "fieldconfigschemeissuetype";
        exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "ISSUETYPE");
        assertTrue("issuetype".equalsIgnoreCase(exactColumnName));

        exactColumnName = UpgradeUtils.getExactColumnName(tableName_issueType, "PNAME");
        assertNull(exactColumnName);

    }

    @Test
    public void testDoColumnsOrTablesExist()
    {
        String tableName_issueType = "issuetype";
        List<TableColumnCheckResult> tableColumnCheckResults = new ArrayList<TableColumnCheckResult>();
        tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "ISSUETYPE"));
        UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
        assertFalse((tableColumnCheckResults.get(0)).isExists());

        tableColumnCheckResults = new ArrayList<TableColumnCheckResult>();
        tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "PNAME"));
        UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
        assertTrue((tableColumnCheckResults.get(0)).isExists());

        tableName_issueType = "fieldconfigschemeissuetype";
        tableColumnCheckResults = new ArrayList<TableColumnCheckResult>();
        tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "ISSUETYPE"));
        UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
        assertTrue((tableColumnCheckResults.get(0)).isExists());

        tableColumnCheckResults = new ArrayList<TableColumnCheckResult>();
        tableColumnCheckResults.add(new TableColumnCheckResult(tableName_issueType, "PNAME"));
        UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
        assertFalse((tableColumnCheckResults.get(0)).isExists());
    }

    @Test
    public void testGetBuildVersionNumber()
    {
        int buildNumber = UpgradeUtils.getJIRABuildVersionNumber();
        assertEquals(Integer.parseInt(buildUtilsInfo.getCurrentBuildNumber()), buildNumber);
    }

    private void alterColumnOnTable(boolean create)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        StringBuilder sql = new StringBuilder();
        try
        {
            connection = ConnectionFactory.getConnection("defaultDS");
            sql.append("ALTER TABLE ");
            sql.append(QRTZ_JOB_DETAILS_TABLE);
            if (create)
            {
                sql.append(" ADD COLUMN ");
                sql.append(DESCRIPTION_COLUMN_NAME);
                sql.append(" VARCHAR ");
            }
            else
            {
                sql.append(" DROP COLUMN ").append(DESCRIPTION_COLUMN_NAME);
            }
            preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    // To late now!
                }
            }
        }

    }

    private void insertIntoTable(String tableName, String columnName, long id, Object value)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        StringBuilder sql = new StringBuilder();
        try
        {
            connection = ConnectionFactory.getConnection("defaultDS");
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" ( ");
            sql.append("id, ");
            sql.append(columnName);
            sql.append(" ) ");
            sql.append(" values ");
            sql.append(" ( ");
            sql.append(" ?, ");
            sql.append(" ?");
            sql.append(" ) ");
            preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.setLong(1, id);
            preparedStatement.setObject(2, value);
            preparedStatement.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    // Oh Well :(
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    // To late now!
                }
            }
        }
    }
}
