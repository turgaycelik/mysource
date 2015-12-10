package com.atlassian.jira.action.admin.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.startup.FormattedLogMsg;
import com.atlassian.jira.startup.JiraSystemInfo;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelViewEntity;

import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.ofbiz.core.entity.EntityFindOptions.findOptions;

public class DefaultSaxEntitiesExporter implements EntitiesExporter
{
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final int DEFAULT_FETCH_SIZE = 1000;

    private static final Logger log = Logger.getLogger(DefaultSaxEntitiesExporter.class);
    @ClusterSafe
    private final ResettableLazyReference<EntityFindOptions> entityFindOptionsRef = new ResettableLazyReference<EntityFindOptions>()
    {
        @Override
        protected EntityFindOptions create() throws Exception
        {
            return getFindOptions();
        }
    };

    private final OfBizDelegator delegator;
    private final ApplicationProperties applicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;
    private final DatabaseConfigurationManager databaseConfigurationManager;


    // WARNING!!!
    // This class is consumed directly by the OnDemand Backup Manager and changing the constructors
    // will break it.  For example:
    //     JRADEV-13777 -> OBM-115
    //     JRADEV-21563 -> OBM-135

    public DefaultSaxEntitiesExporter(final OfBizDelegator delegator, final ApplicationProperties applicationProperties, final BuildUtilsInfo buildUtilsInfo)
    {
        this(delegator, applicationProperties, buildUtilsInfo, ComponentAccessor.getComponent(DatabaseConfigurationManager.class));
    }

    public DefaultSaxEntitiesExporter(final OfBizDelegator delegator, final ApplicationProperties applicationProperties, final BuildUtilsInfo buildUtilsInfo, final DatabaseConfigurationManager databaseConfigurationManager)
    {
        this.delegator = notNull("delegator", delegator);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.databaseConfigurationManager = databaseConfigurationManager;
    }

    public long exportEntities(final OutputStream outputStream, final SortedSet<String> entityNames,
            final EntityXmlWriter entityWriter, final User exportingUser) throws IOException, GenericEntityException
    {
        // To be sure we get a fresh view of the database config and find options, we reset this here.
        entityFindOptionsRef.reset();

        // Force the project cache to be populated - this is required for calculating issue keys
        long projectCount = ComponentAccessor.getProjectManager().getProjectCount();

        final EntityCounter entityCounter = new EntityCounter();
        // Ensure that we use JIRA's encoding for output
        // Buffer 32KB at a time
        final PrintWriter printWriter = getWriter(outputStream);
        writeHeader(printWriter, exportingUser);

        try
        {
            for (final String curEntityName : entityNames)
            {
                log.debug("curEntityName = " + curEntityName);

                final ModelEntity modelEntity = delegator.getModelReader().getModelEntity(curEntityName);
                // Export only normal (non-view) entities
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    exportEntity(curEntityName, entityCounter, entityWriter, printWriter);
                }
                else
                {
                    log.debug("No need to export entity '" + curEntityName + "' as it is a view entity.");
                }
            }
            writeFooter(printWriter, entityCounter);
        }
        finally
        {
            // Do NOT close the writer here!!! The writer is constructed over a stream that is passed in. It is the responsibility
            // of the calling code to close the stream (as the caller must have opened it). If we close the writer here, it will close the
            // underlying stream, and when the caller tries to close the stream they might get an exception. See JRA-4964 (Only fails under jdk 1.3)
            // Flush the buffer so that all the contents is written.
            if (printWriter != null)
            {
                printWriter.flush();
            }
        }
        return entityCounter.total.get();
    }

    private void exportEntity(final String curEntityName, final EntityCounter entityCounter,
            final EntityXmlWriter entityWriter, final PrintWriter printWriter) throws GenericEntityException
    {
        final EntityFindOptions findOptions = getFindOptions();

        boolean inTransaction = false;
        boolean ok = false;
        OfBizListIterator listIterator = null;
        try
        {
            // JRA-28591 Start a transaction to work around memory problems with Postgres and autocommit
            inTransaction = CoreTransactionUtil.begin();
            log.debug("  inTransaction=" + inTransaction);

            listIterator = delegator.findListIteratorByCondition(curEntityName, null, null, null, null, findOptions);
            GenericValue genericValue = listIterator.next();
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (genericValue != null)
            {
                entityWriter.writeXmlText(genericValue, printWriter);
                entityCounter.increment(curEntityName);
                genericValue = listIterator.next();
            }
            ok = true;
        }
        finally
        {
            cleanUpAfterEntity(inTransaction, listIterator, ok);
        }
    }

    private void cleanUpAfterEntity(final boolean inTransaction, final OfBizListIterator listIterator, boolean ok)
            throws GenericEntityException
    {
        try
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }
        finally
        {
            if (inTransaction)
            {
                // commit vs. rollback should make absolutely no difference for a read-only transaction, but we'll
                // go ahead and try both.
                try
                {
                    if (ok)
                    {
                        ok = false;
                        CoreTransactionUtil.commit(true);
                        ok = true;
                    }
                }
                finally
                {
                    if (!ok)
                    {
                        CoreTransactionUtil.rollback(true);
                    }
                }
            }
        }
    }

    protected PrintWriter getWriter(final OutputStream outputStream) throws UnsupportedEncodingException
    {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, applicationProperties.getEncoding()), DEFAULT_BUFFER_SIZE));
    }

    /**
     * This closes out the XML documment.
     *
     * @param printWriter a PrintWriter to write to
     * @param entityCounter a count of entities written
     */
    protected void writeFooter(final PrintWriter printWriter, final EntityCounter entityCounter)
    {
        writeEntityCountComment(printWriter, entityCounter);
        printWriter.write("</entity-engine-xml>");
    }

    /**
     * Writes an XML comment containing entity count information
     *
     * @param printWriter a PrintWriter to us
     * @param entityCounter the enitytCounter with the counts
     */
    private void writeEntityCountComment(final PrintWriter printWriter, final EntityCounter entityCounter)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg();
        try
        {
            entityCounter.outputToMessage(logMsg);
        }
        catch (final RuntimeException rte)
        {
            // This is done as a worst case scenario.  We would rather have the exported datafile than
            // have corrupted system info in it
            log.error("An exception occuring while writing the JIRA system info end comment", rte);
            return;
        }

        printWriter.println("<!-- ");
        printWriter.println(escapeXmlComment(logMsg.toString()));
        printWriter.println(" -->");
    }

    /**
     * This will write the start of the exported XML file out.  This includes the XML declaration and the top level root
     * element
     *
     * @param printWriter a PrintWriter to write to
     * @param exportingUser the user doing the export
     */
    protected void writeHeader(final PrintWriter printWriter, final User exportingUser)
    {
        printWriter.println("<?xml version=\"1.0\" encoding=\"" + applicationProperties.getEncoding() + "\"?>");
        writeSysInfoComment(printWriter, exportingUser);
        printWriter.println(String.format("<entity-engine-xml date=\"%d\">", System.currentTimeMillis()));
    }

    /**
     * This writes a XML comment to the start of the exported XML indicating the JIRA system information at the time the
     * data was exported.
     *
     * @param printWriter the printwriter to write to
     * @param exportingUser the use who is requesting the export
     */
    private void writeSysInfoComment(final PrintWriter printWriter, final User exportingUser)
    {
        final FormattedLogMsg logMsg = new FormattedLogMsg();
        try
        {
            final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            final String when = df.format(new Date());
            String exportingUserName = "Unknown???";
            if (exportingUser != null)
            {
                exportingUserName = exportingUser.getName();
                if (exportingUser.getDisplayName() != null)
                {
                    exportingUserName += " ( " + exportingUser.getDisplayName() + " )";
                }
            }
            logMsg.outputHeader("Exported on");
            logMsg.outputProperty("on", when);
            logMsg.outputProperty("by", exportingUserName);

            final JiraSystemInfo info = new JiraSystemInfo(logMsg, buildUtilsInfo);
            info.obtainBasicInfo(null);
            info.obtainDatabaseConfigurationInfo();
            info.obtainJiraAppProperties();
            info.obtainDatabaseStatistics();
            info.obtainUpgradeHistory();
            info.obtainFilePaths();
            info.obtainPlugins();
            info.obtainListeners();
            info.obtainServices();
            info.obtainTrustedApps();
        }
        catch (final RuntimeException rte)
        {
            // This is done as a worst case scenario.  We would rather have the exported datafile than
            // have corrupted system info in it
            log.error("An exception occuring while writing the JIRA system info start commment", rte);
            return;
        }
        printWriter.println("<!-- ");
        printWriter.println(escapeXmlComment(logMsg.toString()));
        printWriter.println(" -->");
    }

    /**
     * Just in case the logged data has a closing --> XML comment in it.
     * <p/>
     * I put this in because of Dylan's threat that Anton would never forgive me if I broke data export / import!
     * <p/>
     * Update : JRA-15753 - but guess what it did any ways!
     * <p/>
     * Rules are at http://www.w3.org/TR/REC-xml/#dt-comment
     *
     * @return an escaped version so that the XML Comment stays just
     */
    private String escapeXmlComment(final String xmlComment)
    {
        String escapedComment = xmlComment;
        if (xmlComment.contains("--"))
        {
            escapedComment = escapedComment.replaceAll("--", "-:");
            // and some explanation
            escapedComment = "\nThe comment data contained one of more occurences of a '-' character followed immediately by another '-' character." +
                    "\nThis is not allowed according to http://www.w3.org/TR/REC-xml/#dt-comment." +
                    "\nThese have been replaced by '-:' characters to make the XML valid\n\n" + escapedComment;
        }
        return escapedComment;
    }

    EntityFindOptions getFindOptions()
    {
        final DatabaseConfig dbConfig = databaseConfigurationManager.getDatabaseConfiguration();
        int fetchSize = getIntProperty(applicationProperties, APKeys.Export.FETCH_SIZE, -1);
        if (fetchSize == -1)
        {
            if (dbConfig.isMySql())
            {
                // JRA-28591 Use streaming mode on MySQL to stop OOMEs.  Nothing else seems to do any good. :(
                fetchSize = Integer.MIN_VALUE;
            }
            else
            {
                fetchSize = DEFAULT_FETCH_SIZE;
            }
        }
        return findOptions().fetchSize(fetchSize);
    }

    /**
     * A simple class to count the entities as they go out the door
     */
    private static final class EntityCounter
    {
        Map<String, AtomicLong> map = new LinkedHashMap<String, AtomicLong>();
        AtomicLong total = new AtomicLong(0);

        private void increment(final String entityName)
        {
            AtomicLong count = map.get(entityName);
            if (count == null)
            {
                count = new AtomicLong(0);
                map.put(entityName, count);
            }
            count.incrementAndGet();
            total.incrementAndGet();
        }

        private void outputToMessage(final FormattedLogMsg logMsg)
        {
            logMsg.outputHeader("Entities");
            logMsg.outputProperty("Total", String.valueOf(total));
            logMsg.add("");
            for (final Map.Entry<String, AtomicLong> entry : map.entrySet())
            {
                logMsg.outputProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }
}
