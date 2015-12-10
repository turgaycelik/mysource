package com.atlassian.jira.startup;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.FieldMap;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * A temporary class to empty the obsolete NotificaionInstance table with a delay.
 *
 * @since v5.2
 */
public class NotificationInstanceKiller
{
    public static final String NOTIFICATION_INSTANCE_UPGRADE_DATE = "notification.instance.upgrade.date";

    private static final long DELAY = 28L * 24 * 60 * 60 * 1000; // 28 days
    private static final Logger log = Logger.getLogger(NotificationInstanceKiller.class);

    /**
     * Will empty the NotificationInstance table after a reasonable delay (eg 4 weeks) from the time this instance upgraded
     * to v5.2 or higher.
     */
    public void deleteAfterDelay()
    {
        String upgradeDateAsText = getApplicationProperties().getString(NOTIFICATION_INSTANCE_UPGRADE_DATE);
        if (upgradeDateAsText == null || upgradeDateAsText.isEmpty())
            return;
        long timeToDie = Long.parseLong(upgradeDateAsText) + DELAY;
        if (System.currentTimeMillis() > timeToDie)
        {
            log.info("Starting a background thread to clear out obsolete NotificationInstance data ...");
            // Kill with extreme prejudice
            new Thread(new Assassin()).start();
        }
        else
        {
            log.info("Not deleting NotificationInstance table yet, will delete on first startup after " + new Date(timeToDie));
        }
    }

    private ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    private class Assassin implements Runnable
    {
        @Override
        public void run()
        {
            log.info("Deleting data from NotificationInstance.");
            // JRA-30293 Unfortunately we need to keep incoming message-IDs, but we can still delete outgoing records
            // which are > 99% of the rows.
            for (int i = 1; i <= 17; i++)
            {
                String type = "NOTIFICATION_" + i;
                log.info("Deleting type " + type + " from NotificationInstance.");
                int count = ComponentAccessor.getOfBizDelegator().removeByAnd("NotificationInstance", FieldMap.build("type", type));
                log.info("Deleted " + count + " rows of type " + type + " from NotificationInstance.");
            }
            log.info("Deleting data from NotificationInstance completed successfully.");
            // we don't have to do this again so remove property
            getApplicationProperties().setString(NOTIFICATION_INSTANCE_UPGRADE_DATE, null);
        }
    }
}
