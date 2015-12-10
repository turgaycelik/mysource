/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.concurrent.ThreadFactories;
import org.apache.log4j.Logger;
import org.hsqldb.DatabaseManager;
import org.hsqldb.lib.HsqlTimer;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to keep connections open to the database while JIRA is running. It is used with HSQLDB as HSQLDB,
 * when running in-process, will shutdown as soon as no connections are open to it. When a new connection is opened to
 * the HSQLDB it will start up again. However, this causes problems as more than one HSQLDB instance may be created at
 * the same time and the two instances interfere wuth each other, possibly causing data loss.
 * <p/>
 * As advised in the HSQLDB documentation, if HSQLDB is used in-process, a connection to it should be open at all times,
 * to prevent the database from shutting down. One connections seems not to be good enough, though (probably due to a
 * HSQLDB bug). Two connections does the trick.
 */
@ClusterSafe("We don't and can't use HSQL in a clustered deployment.")
public final class ConnectionKeeper
{
    private static final Logger log = Logger.getLogger(ConnectionKeeper.class);

    private final String ofbizHelperName;
    private final List<Connection> connections;
    private final int numberConnections;
    private final int sleepMillis;
    private final ScheduledExecutorService executor;
    private boolean running;

    public ConnectionKeeper(final String ofbizHelperName, final int numberConnections, final int sleepMillis)
    {
        this.ofbizHelperName = ofbizHelperName;
        this.numberConnections = numberConnections;
        this.sleepMillis = sleepMillis;
        connections = openConnections();
        executor = Executors.newSingleThreadScheduledExecutor(
                ThreadFactories.namedThreadFactory("ConnectionKeeper"));
    }

    /**
     * Schedule the connection keeper.  This isn't done in a constructor because doing it in a constructor would mean
     * passing a reference to ourselves to another thread before the constructor had finished constructing... this is
     * bad practice.
     */
    public synchronized void start()
    {
        running = true;
        executor.scheduleWithFixedDelay(new ConnectionKeeperCommand(), 0, sleepMillis, TimeUnit.MILLISECONDS);
    }

    private class ConnectionKeeperCommand implements Runnable
    {
        public void run()
        {
            synchronized (ConnectionKeeper.this)
            {
                if (running)
                {
                    final List<Connection> temp = new ArrayList<Connection>(connections);

                    // Open connection(s) before closing the old one(s) to ensure the connection(s)
                    // stay open at all times
                    connections.clear();
                    connections.addAll(openConnections());
                    closeAllConnections(temp, false);
                }
            }
        }
    }

    public synchronized void shutdown()
    {
        running = false;
        executor.shutdown();
        closeAllConnections(connections, true);
        killTimerThread();
    }

    /**
     * JRADEV-5023: Make sure the timer tread is killed. If we don't kill this thread tomcat will clear all static
     * variables as the webapp is destroyed which in turn may cause a NPE for this particular thread.
     */
    private void killTimerThread()
    {
        final HsqlTimer timer = DatabaseManager.getTimer();
        if (timer != null)
        {
            Thread timerThread = timer.getThread();
            timer.shutDown();
            if (timerThread != null)
            {
                try
                {
                    timerThread.join(TimeUnit.SECONDS.toMillis(30));
                    if (timerThread.isAlive())
                    {
                        log.warn("Timed out while waiting for HSQL timer to shutdown.");
                    }
                }
                catch (InterruptedException e)
                {
                    log.warn("Interrupted while waiting for HSQL timer to shutdown.");
                }
            }
        }
    }

    private List<Connection> openConnections()
    {
        final List<Connection> connectionList = new ArrayList<Connection>();
        Connection connection;
        for (int i = 0; i < numberConnections; i++)
        {
            try
            {
                connection = ConnectionFactory.getConnection(ofbizHelperName);
                connectionList.add(connection);
            }
            catch (final SQLException e)
            {
                log.error("Could not open connection.", e);
            }
            catch (final GenericEntityException e)
            {
                log.error("Could not open connection.", e);
            }
        }
        return connectionList;
    }

    /**
     * This will close all open connections to the HSQL DB instance. If a shutdown command has been issued we'll also
     * shutdown HSQL DB on the final connection.
     *
     * @param connectionList The connections to close
     * @param shutdownHSQL If HSQL should be shutdown
     */
    private void closeAllConnections(final List<Connection> connectionList, final boolean shutdownHSQL)
    {
        for (final Iterator<Connection> iterator = connectionList.iterator(); iterator.hasNext();)
        {
            final Connection connection = iterator.next();
            try
            {
                if ((connection != null) && !connection.isClosed())
                {
                    // If we are shutting down jira AND we have the last Connection, shutdown HSQL DB.
                    if (shutdownHSQL && !iterator.hasNext())
                    {
                        shutdownHsql(connection);
                    }

                    connection.close();
                    log.debug("Closed connection.");
                }
                else
                {
                    log.warn("Connection was closed or not initialised properly.");
                }
            }
            catch (final SQLException e)
            {
                log.error("Error closing connection.", e);
            }
        }
    }

    /**
     * Calls "SHUTDOWN" on the hsql database in accordance with docs here: http://www.hsqldb.org/doc/guide/ch01.html#N101DB
     *
     * @param connection The connection to shutdown
     * @throws SQLException If an error occured
     */
    private void shutdownHsql(final Connection connection) throws SQLException
    {
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
            statement.execute("SHUTDOWN");
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
        }
    }
}
