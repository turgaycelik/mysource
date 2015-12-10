package com.atlassian.jira.util.system.check;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.HelpUtil;
import com.google.common.annotations.VisibleForTesting;
import org.ofbiz.core.entity.config.JndiDatasourceInfo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * If they are using MySQL and if they are using tomcat (or DBCP in some other way) then we can check if they have a
 * validationQuery which is recommended to prevent data corruption when connections time out.
 *
 * @since v4.2
 */
public class JRA15731Check implements SystemEnvironmentCheck
{
    private final OfBizConnectionFactory factory;

    JRA15731Check(OfBizConnectionFactory factory)
    {
        this.factory = Assertions.notNull(factory);
    }

    public JRA15731Check()
    {
        this(DefaultOfBizConnectionFactory.getInstance());
    }

    public String getName()
    {
        return "MySQL Validation Query Check";
    }

    public I18nMessage getWarningMessage()
    {
        if (isMySQL() && !hasValidationQuery())
        {
            HelpUtil helpUtil = new HelpUtil();

            I18nMessage warning = new I18nMessage("admin.warning.JRA15731.syscheck");
            warning.setLink(helpUtil.getHelpPath("JRA15731").getUrl());
            return warning;
        }
        else
        {
            return null;
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
            return dbMeta.getDatabaseProductName().equals("MySQL");
        }
        catch (SQLException e)
        {
            return false;
        }
        finally
        {
            silentlyClose(connection);
        }
    }

    // This code relies on DBCP (which is what tomcat uses). That's why we do things reflectively, because we don't
    // want to have any compile time dependencies on tomcat. We also need to ensure that we are careful to handle
    // failures along the way "safely" in case they are running on some other app server.

    private boolean hasValidationQuery()
    {
        final JndiDatasourceInfo jndi = factory.getDatasourceInfo().getJndiDatasource();
        if (jndi != null)
        {
            // We are very lenient here. If we come across any error whatsoever then we return true (i.e. there is a validation
            // query. We are being paranoid about things that might go wrong and generate spurious and confusing error messages.
            // And we *definitely* don't want anything in here to prevent JIRA from starting up.
            try
            {
                Context initCtx = new InitialContext();
                Object resource = initCtx.lookup(jndi.getJndiName());
                if (!(resource instanceof DataSource))
                {
                    return true;
                }
                for (Method method : resource.getClass().getMethods())
                {
                    if (method.getName().equals("getValidationQuery"))
                    {
                        try
                        {
                            final String validationQuery = (String) method.invoke(resource);
                            return validationQuery != null;
                        }
                        catch (IllegalAccessException e)
                        {
                            return true;
                        }
                        catch (InvocationTargetException e)
                        {
                            return true;
                        }
                    }
                }
                return true;
            }
            catch (NamingException e)
            {
                return true;
            }
        }
        return true;
    }

    private void silentlyClose(Connection connection)
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
