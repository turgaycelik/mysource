package com.atlassian.jira.upgrade.tasks;

import java.sql.Connection;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.Sequences;

/**
 * Add a correctly initialised sequence for EventType
 *
 * @since 6.3
 */
public class UpgradeTask_Build6320 extends AbstractUpgradeTask
{
    private final Sequences sequences;

    public UpgradeTask_Build6320(Sequences sequences)
    {
        super(false);
        this.sequences = sequences;
    }

    @Override
    public String getShortDescription()
    {
        return "Insert sequence for event type";
    }

    @Override
    public String getBuildNumber()
    {
        return "6320";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        Connection connection = getDatabaseConnection();
        try
        {
            sequences.update(connection, "EventType", "jiraeventtype");
        }
        finally
        {
            connection.close();
        }
    }
}
