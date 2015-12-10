package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.Sequences;

import java.sql.Connection;

/**
 * Corrects the value of the &quot;Membership&quot; database sequence. See JRA-24466.
 *
 * @since v4.4
 */
public class UpgradeTask_Build640 extends AbstractUpgradeTask
{
    private final Sequences sequences;

    public UpgradeTask_Build640(final Sequences sequences)
    {
        super(false);
        this.sequences = sequences;
    }

    @Override
    public String getBuildNumber()
    {
        return "640";
    }

    @Override
    public String getShortDescription()
    {
        return "Corrects the value of the 'Membership' database sequence. See JRA-24466.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Connection connection = getDatabaseConnection();

        boolean committed = false;
        try
        {
            connection.setAutoCommit(false);

            sequences.update(connection, "Membership", "cwd_membership");

            connection.commit();
            committed = true;
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
}
