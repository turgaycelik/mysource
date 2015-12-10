package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.upgrade.util.UpgradeUtils;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Build178SchemaCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(Build178SchemaCheck.class);

    private static final String INSTRUCTIONS_URL = ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.3.7.upgrade.guide");

    private static final String FAULT_DESC = "We have detected that JIRA is configured to " +
            "connect to a database from a previous version of JIRA. If you have just upgraded " +
            "JIRA, you may need to consult <a href=\"" + INSTRUCTIONS_URL + "\">the 3.7 Upgrade Guide</a>.";
    private static final String NAME = "Build178SchemaCheck";

    private boolean isOk = true;

    public String getFaultDescription()
    {
        return FAULT_DESC;
    }

    public String getHTMLFaultDescription()
    {
        return getFaultDescription();
    }

    @Override
    public void stop()
    {
    }

    public String getName()
    {
        return NAME;
    }

    public boolean isOk()
    {
        log.debug("Checking database...");
        List<TableColumnCheckResult> tableColumnCheckResults = new ArrayList<TableColumnCheckResult>();

        // Add the columns that we want to check
        tableColumnCheckResults.add(new TableColumnCheckResult("notification", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("notification", "parameter"));
        tableColumnCheckResults.add(new TableColumnCheckResult("mailserver", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("jiraeventtype", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("schemepermissions", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("schemeissuesecurities", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("fieldlayout", "type"));
        tableColumnCheckResults.add(new TableColumnCheckResult("SchemePermissions", "parameter"));
        tableColumnCheckResults.add(new TableColumnCheckResult("SchemeIssueSecurities", "parameter"));
        tableColumnCheckResults.add(new TableColumnCheckResult("PortletConfiguration", "position"));

        // Add the table that we want to check
        tableColumnCheckResults.add(new TableColumnCheckResult("version"));

        doColumnTableChecks(tableColumnCheckResults);

        checkColumnsAreInTables(tableColumnCheckResults);
        checkTableDoesNotExist("version", tableColumnCheckResults);
        log.debug("Database checks finished.");
        // do we need to check for "version" ?
        return isOk;
    }

    private void checkColumnsAreInTables(List<TableColumnCheckResult> tableColumnCheckResults)
    {
        for (TableColumnCheckResult tableColumnCheckResult : tableColumnCheckResults)
        {
            isOk = !tableColumnCheckResult.isExists();
            if (!isOk)
            {
                log.fatal("Found column: '" + tableColumnCheckResult.getColumnName() + "' in table: '"
                        + tableColumnCheckResult.getTableName() + "', this column should not be present.");
                break;
            }
        }
    }

    private void checkTableDoesNotExist(String table, List<TableColumnCheckResult> tableColumnCheckResults)
    {
        if (isOk)
        {
            for (TableColumnCheckResult tableColumnCheckResult : tableColumnCheckResults)
            {
                if (tableColumnCheckResult.getTableName().equalsIgnoreCase(table))
                {
                    isOk = !tableColumnCheckResult.isExists();
                    if (!isOk)
                    {
                        log.fatal("Found table '" + table + "', this table should not be present.");
                    }
                }
            }
        }
    }

    void doColumnTableChecks(List<TableColumnCheckResult> tableColumnCheckResults)
    {
        // Perform the check
        UpgradeUtils.doColumnsOrTablesExist(tableColumnCheckResults);
    }

    @Override
    public String toString()
    {
        return NAME;
    }
}
