package com.atlassian.jira.scheduler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.exception.DataAccessException;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class supplies a database connection to the Quartz scheduler
 *
 * @since v6.2
 */
public class Quartz1ConnectionProvider implements org.quartz.utils.ConnectionProvider
{
    private final ComponentReference<DelegatorInterface> delegatorRef = ComponentAccessor.getComponentReference(DelegatorInterface.class);

    @Override
    public Connection getConnection() throws SQLException
    {
        try
        {
            GenericHelper helper = delegatorRef.get().getEntityHelper("JQRTZJobDetails");
            DatabaseUtil utils = new DatabaseUtil(helper.getHelperName());
            return utils.getConnection();
        }
        catch (SQLException e)
        {
            throw new DataAccessException("Unable to obtain a DB connection", e);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Unable to obtain a DB connection", e);
        }
    }

    @Override
    public void shutdown() throws SQLException
    {
    }
}
