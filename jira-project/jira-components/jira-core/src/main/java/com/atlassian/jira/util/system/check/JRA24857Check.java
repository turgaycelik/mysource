package com.atlassian.jira.util.system.check;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.HelpUtil;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.model.ModelViewEntity;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * If they are using MySQL we need to warn people if they ain't using InnoDB.
 *
 * @since v4.4.1
 */
public class JRA24857Check implements SystemEnvironmentCheck
{
    private static final Logger log = Logger.getLogger(JRA24857Check.class);
    private static final String ENGINE = "MyISAM";

    private final OfBizConnectionFactory factory;
    private final DelegatorInterface delegatorInterface;

    @VisibleForTesting
    JRA24857Check(OfBizConnectionFactory factory, DelegatorInterface delegatorInterface)
    {
        this.delegatorInterface = delegatorInterface;
        this.factory = Assertions.notNull(factory);
    }

    public JRA24857Check()
    {
        this(DefaultOfBizConnectionFactory.getInstance(), ComponentAccessor.getComponent(DelegatorInterface.class));
    }

    public String getName()
    {
        return "MySQL MyISAM Check";
    }

    public I18nMessage getWarningMessage()
    {
        if (isMySQL())
        {
            Set<String> jiraTablesUsingMyISAM = getJiraTablesUsingMyISAM();
            if (!jiraTablesUsingMyISAM.isEmpty() || isSessionUsingMySIAM())
            {
                return createWarning();
            }
        }
        return null;
    }

    private Set<String> getJiraTablesUsingMyISAM()
    {
        Set<String> badTables = getMyISAMTables();
        badTables.retainAll(getJiraTables());

        return badTables;
    }

    @VisibleForTesting
    protected I18nMessage createWarning()
    {
        HelpUtil helpUtil = new HelpUtil();

        I18nMessage warning = new I18nMessage("admin.warning.JRA24857.syscheck");
        warning.setLink(helpUtil.getHelpPath("JRA24857").getUrl());
        return warning;
    }

    /**
     * Detect if the current session will create tables using MyISAM.
     *
     * @return true if JIRA will create new tables using MyISAM or false otherwise.
     */
    @VisibleForTesting
    boolean isSessionUsingMySIAM()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try
        {
            connection = factory.getConnection();

            //This query only works from 5.0.x. MySQL < 5.0.x was is not supported by MySQL at the time of writing so I think this is fine.
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = statement.executeQuery("SELECT @@storage_engine as engine");
            if (resultSet.next())
            {
                return ENGINE.equalsIgnoreCase(trimToNull(resultSet.getString(1)));
            }
            else
            {
                log.warn("Unable to detect MySQL engine type. Assuming correct engine type.");
                return false;
            }
        }
        catch (SQLException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to detect MySQL engine type.", e);
            }
            else
            {
                log.warn("Unable to detect MySQL engine type. Assuming correct engine type.");
            }
            return false;
        }
        finally
        {
            silentlyClose(resultSet);
            silentlyClose(statement);
            silentlyClose(connection);
        }
    }

    /**
     * Return a collection of the current tables that exist under MyISAM.
     *
     * @return a collection of tables in configured database that exist under the MyISAM engine.
     */
    @VisibleForTesting
    Set<String> getMyISAMTables()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            connection = factory.getConnection();
            String catalog = connection.getCatalog();

            //This query only works from 5.0.x. MySQL < 5.0.x was is not supported by MySQL at the time of writing so I think this is fine.
            statement = connection.prepareStatement("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = ? and engine = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, catalog);
            statement.setString(2, ENGINE);

            resultSet = statement.executeQuery();
            Set<String> set = createTableSet();
            while (resultSet.next())
            {
                set.add(trimToNull(resultSet.getString(1)));
            }

            return set;
        }
        catch (SQLException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to read DB metadata from INFROMATION_SCHEMA.", e);
            }
            else
            {
                log.warn("Unable to read DB metadata from INFROMATION_SCHEMA. Assuming correct MySQL engine.");
            }
            return Collections.emptySet();
        }
        finally
        {
            silentlyClose(resultSet);
            silentlyClose(statement);
            silentlyClose(connection);
        }
    }

    /**
     * Return a collection of all JIRA's configured database tables.
     *
     * @return a collection of all JIRA's configured database tables.
     */
    @VisibleForTesting
    Set<String> getJiraTables()
    {
        final ModelReader modelReader = delegatorInterface.getModelReader();
        try
        {
            final Set<String> tables = createTableSet();
            for (String name : modelReader.getEntityNames())
            {
                ModelEntity modelEntity = modelReader.getModelEntity(name);
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    tables.add(modelEntity.getPlainTableName());
                }
            }
            return tables;
        }
        catch (GenericEntityException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to read DB configuration.", e);
            }
            else
            {
                log.warn("Unable to read DB configuration. Assuming correct MySQL engine.");
            }
            return Collections.emptySet();
        }
    }

    @VisibleForTesting
    boolean isMySQL()
    {
        Connection connection = null;
        try
        {
            connection = factory.getConnection();
            final DatabaseMetaData dbMeta = connection.getMetaData();
            final String productName = dbMeta.getDatabaseProductName();
            return productName != null && productName.contains("MySQL");
        }
        catch (SQLException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to detect database type.", e);
            }
            else
            {
                log.warn("Unable to detect database type. Assuming not MySQL.");
            }

            return false;
        }
        finally
        {
            silentlyClose(connection);
        }
    }

    private static Set<String> createTableSet()
    {
        return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    }

    private static void silentlyClose(ResultSet resultSet)
    {
        if (resultSet != null)
        {
            try
            {
                resultSet.close();
            }
            catch (SQLException ignored)
            {
            }
        }
    }

    private static void silentlyClose(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (SQLException ignored)
            {
            }
        }
    }

    private static void silentlyClose(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException ignored)
            {
            }
        }
    }
}
