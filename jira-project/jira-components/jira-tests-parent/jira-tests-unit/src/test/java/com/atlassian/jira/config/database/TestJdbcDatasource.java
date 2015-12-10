package com.atlassian.jira.config.database;

import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

/**
 * Unit test for {@link JdbcDatasource}.
 *
 * @since v4.4
 */
public class TestJdbcDatasource
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    public static final String JDBC_URL = "url";
    public static final String DRIVER_CLASS_NAME = "java.lang.String";
    public static final String USERNAME = "whoYoDaddy";
    public static final String PASSWORD = "pssst";
    public static final int POOL_MAX_SIZE = 31337;
    
    private ConnectionPoolInfo getPoolInfoWithMaxSize(int maxSize)
    {
        return ConnectionPoolInfo.builder().setPoolMaxSize(maxSize).build();
    }

    @Test
    public void createDbDetails()
    {
        JdbcDatasource datasource = JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
        final DatabaseDetails dbDetails = datasource.createDbDetails();
        Assert.assertEquals(JDBC_URL, dbDetails.getDatabaseUrl());
        Assert.assertEquals(DRIVER_CLASS_NAME, dbDetails.getDriverClassName());
        Assert.assertEquals(USERNAME, dbDetails.getUserName());
        Assert.assertEquals(PASSWORD, dbDetails.getPassword());
        Assert.assertEquals(POOL_MAX_SIZE, dbDetails.getPoolSize());
    }

    @Test
    public void deprecatedConstructorBlankPasswordOk()
    {
        new JdbcDatasource(JDBC_URL, DRIVER_CLASS_NAME, USERNAME, "", POOL_MAX_SIZE, null, null, null);
    }

    @Test
    public void builderBlankPasswordOk()
    {
        JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername(USERNAME)
                .setPassword("")
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
    }

    @Test (expected = IllegalArgumentException.class)
    public void deprecatedConstructorNegativePoolSize()
    {
        new JdbcDatasource(JDBC_URL, DRIVER_CLASS_NAME, USERNAME, PASSWORD, -POOL_MAX_SIZE, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void builderNegativePoolSize()
    {
        JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(-POOL_MAX_SIZE))
                .build();
    }

    @Test (expected = IllegalArgumentException.class)
    public void deprecatedConstructorNullUrl()
    {
        new JdbcDatasource(null, DRIVER_CLASS_NAME, USERNAME, PASSWORD, POOL_MAX_SIZE, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void builderNullUrl()
    {
        JdbcDatasource.builder()
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
    }


    @Test (expected = IllegalArgumentException.class)
    public void deprecatedConstructorNullDriver()
    {
        new JdbcDatasource(JDBC_URL, null, USERNAME, PASSWORD, POOL_MAX_SIZE, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void builderNullDriver()
    {
        JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
    }

    @Test (expected = IllegalArgumentException.class)
    public void deprecatedConstructorBlankUsername()
    {
        new JdbcDatasource(JDBC_URL, DRIVER_CLASS_NAME, "", PASSWORD, POOL_MAX_SIZE, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void builderBlankUsername()
    {
        JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername("")
                .setPassword(PASSWORD)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
    }

    @Test (expected = IllegalArgumentException.class)
    public void deprecatedConstructorNullPassword()
    {
        new JdbcDatasource(JDBC_URL, DRIVER_CLASS_NAME, USERNAME, null, POOL_MAX_SIZE, null, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void builderNullPassword()
    {
        JdbcDatasource.builder()
                .setJdbcUrl(JDBC_URL)
                .setDriverClassName(DRIVER_CLASS_NAME)
                .setUsername(USERNAME)
                .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                .build();
    }

    @Test(expected = InvalidDatabaseDriverException.class)
    public void throwsIllegalStateExceptionIfDbDriverCannotBeLoaded()
    {
        JdbcDatasource.builder()
                        .setJdbcUrl(JDBC_URL)
                        .setDriverClassName("this.cannot.exist.Pleaase")
                        .setUsername(USERNAME)
                        .setPassword(PASSWORD)
                        .setConnectionPoolInfo(getPoolInfoWithMaxSize(POOL_MAX_SIZE))
                        .build();
    }
}
