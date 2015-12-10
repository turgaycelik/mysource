package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.UserPropertyManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

/**
 * Manages the user properties for admin tasks.
 *
 * @since v6.0
 */
public class AdminTaskUserPropertyManager
{
    private static final Logger log = Logger.getLogger(AdminTaskUserPropertyManager.class);

    private static final String TASK_LIST_STATE_KEY_PREFIX = "jira.admingadget.tasklist.";
    private static final String TASK_STATE_KEY_PREFIX = "jira.admingadget.task.";
    private static final String LOOK_AND_FEEL_KEY = "jira.admingadget.lookandfeel.updated";

    private final UserPropertyManager userPropertyManager;
    private final ApplicationProperties applicationProperties;

    public AdminTaskUserPropertyManager(final UserPropertyManager userPropertyManager, final ApplicationProperties applicationProperties)
    {
        this.userPropertyManager = userPropertyManager;
        this.applicationProperties = applicationProperties;
    }

    public void setTaskListDimissed(final User user, final String taskListName, final boolean dismissed)
    {
        if (isUpgraded()) {
            setBoolean(user, TASK_LIST_STATE_KEY_PREFIX + taskListName, !dismissed);
        } else {
            setBoolean(user, TASK_LIST_STATE_KEY_PREFIX + taskListName, dismissed);
        }
    }

    public boolean isTaskListDismissed(final User user, final String taskListName)
    {
        boolean taskListDismissedStore = getBoolean(user, TASK_LIST_STATE_KEY_PREFIX + taskListName);
        if (isUpgraded()) {
            return !taskListDismissedStore;
        } else {
            return taskListDismissedStore;
        }
    }

    public void setTaskMarkedAsCompleted(final User user, final String taskName, final boolean done)
    {
        setBoolean(user, TASK_STATE_KEY_PREFIX + taskName, done);
    }

    public boolean hasTaskCompletedProperty(final User user, final String taskName)
    {
        return hasProperty(user, TASK_STATE_KEY_PREFIX + taskName);
    }

    public boolean isTaskMarkedAsCompleted(final User user, final String taskName)
    {
        return getBoolean(user, TASK_STATE_KEY_PREFIX + taskName);
    }

    public void setLookAndFeelUpdated(final User user, final boolean updated)
    {
        setBoolean(user, LOOK_AND_FEEL_KEY, updated);
    }

    public boolean isLookAndFeelUpdated(final User user)
    {
        return getBoolean(user, LOOK_AND_FEEL_KEY);
    }

    private boolean hasProperty(final User user, final String propertyKey)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to get '" + propertyKey + "' preference for user: " + user);
            return false;
        }

        return ps.exists(propertyKey);
    }

    private boolean getBoolean(final User user, final String propertyKey)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to get '" + propertyKey + "' preference for user: " + user);
            return false;
        }

        return ps.getBoolean(propertyKey);
    }

    private void setBoolean(final User user, final String propertyKey, final boolean value)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to set '" + propertyKey + "' preference for user: " + user);
            return;
        }

        ps.setBoolean(propertyKey, value);
    }

    private boolean isUpgraded() {
        return !applicationProperties.getOption(APKeys.JIRA_ADMIN_GADGET_TASK_LIST_ENABLED);
    }
}
