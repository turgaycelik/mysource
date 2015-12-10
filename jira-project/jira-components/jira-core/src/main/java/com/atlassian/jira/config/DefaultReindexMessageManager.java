package com.atlassian.jira.config;

import java.io.Serializable;
import java.util.Date;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.opensymphony.module.propertyset.PropertySet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the Reindex Message Manager that uses a simple PropertySet to store the last message
 * pushed.
 *
 * @since v4.0
 */
@EventComponent
public class DefaultReindexMessageManager implements ReindexMessageManager, Startable
{
    private static final String MESAGE_LOCK_NAME = DefaultReindexMessageManager.class.getName() + ".messageLock";

    static final String PS_KEY = "admin.message.manager";
    static final String PS_KEY_USER = "user";
    static final String PS_KEY_TASK = "task";
    static final String PS_KEY_TIME = "time";

    static final String PS_KEY_RAW = "rawmsg";
    private final UserKeyService userKeyService;
    private final UserFormatManager userFormatManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final OutlookDateManager outlookDateManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final CachedReference<PropertySet> propertiesReference;
    private final PermissionManager permissionManager;
    private final TaskManager taskManager;
    private final ClusterLockService clusterLockService;

    private volatile ClusterLock messageLock;

    public DefaultReindexMessageManager(final JiraPropertySetFactory jiraPropertySetFactory,
            final UserKeyService userKeyService, final UserFormatManager userFormatManager,
            final I18nHelper.BeanFactory i18nFactory, final OutlookDateManager outlookDateManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final PermissionManager permissionManager, final TaskManager taskManager, final CacheManager cacheManager, final ClusterLockService clusterLockService)
    {
        this.clusterLockService = clusterLockService;
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.velocityRequestContextFactory = notNull("velocityRequestContextFactory", velocityRequestContextFactory);
        this.userKeyService = notNull("userKeyService", userKeyService);
        this.userFormatManager = notNull("userFormatManager", userFormatManager);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.outlookDateManager = notNull("outlookDateManager", outlookDateManager);
        this.taskManager = notNull("taskManager", taskManager);
        this.propertiesReference = cacheManager.getCachedReference(DefaultReindexMessageManager.class, "propertiesReference",
                new Supplier<PropertySet>()
                {
                    @Override
                    public PropertySet get()
                    {
                        return jiraPropertySetFactory.buildCachingDefaultPropertySet(PS_KEY, true);
                    }
                });
    }

    @Override
    public void start()
    {
        messageLock = clusterLockService.getLockForName(MESAGE_LOCK_NAME);
    }

    @EventListener
    @SuppressWarnings("UnusedParameter")
    public void onClearCache(final ClearCacheEvent event)
    {
        messageLock.lock();
        try
        {
            propertiesReference.reset();
        }
        finally
        {
            messageLock.unlock();
        }
    }

    @Override
    public void pushMessage(final User user, final String i18nTask)
    {
        messageLock.lock();
        try
        {
            pushMessage(user);
            propertiesReference.get().setString(PS_KEY_TASK, i18nTask);
            propertiesReference.reset();
        }
        finally
        {
            messageLock.unlock();
        }
    }

    @Override
    public void pushRawMessage(final User user, final String i18nMessage)
    {
        messageLock.lock();
        try
        {
            pushMessage(user);
            propertiesReference.get().setString(PS_KEY_RAW, i18nMessage);
            propertiesReference.reset();
        }
        finally
        {
            messageLock.unlock();
        }
    }

    private void pushMessage(final User user)
    {
        final String key = ApplicationUsers.getKeyFor(user);
        propertiesReference.get().setString(PS_KEY_USER, key == null ? "" : key);
        propertiesReference.get().setDate(PS_KEY_TIME, getCurrentDate());
        propertiesReference.reset();
    }

    @Override
    public void clear()
    {
        messageLock.lock();
        try
        {
            final PropertySet propertySet = propertiesReference.get();
            if (propertySet.exists(PS_KEY_USER))
            {
                propertySet.remove(PS_KEY_TIME);
                if (propertySet.exists(PS_KEY_TASK))
                {
                    propertySet.remove(PS_KEY_TASK);
                }
                propertySet.remove(PS_KEY_USER);
                if (propertySet.exists(PS_KEY_RAW))
                {
                    propertySet.remove(PS_KEY_RAW);
                }
            }
            propertiesReference.reset();
        }
        finally
        {
            messageLock.unlock();
        }
    }

    @Override
    public String getMessage(final User user)
    {
        messageLock.lock();
        try
        {
            TaskDescriptor<Serializable> task = findActiveIndexTasks();

            final PropertySet propertySet = propertiesReference.get();
            if (propertySet.exists(PS_KEY_USER) && permissionManager.hasPermission(Permissions.ADMINISTER, user))
            {
                final String userKey = propertySet.getString(PS_KEY_USER);
                final String i18nTaskKey = propertySet.getString(PS_KEY_TASK);
                final Date time = propertySet.getDate(PS_KEY_TIME);
                final String rawMessageKey = propertySet.getString(PS_KEY_RAW);

                if (rawMessageKey != null && rawMessageKey.length()>0)
                {
                    return getRawIndexRequiredMessage(user, rawMessageKey);
                }
                else
                {
                    if (task == null || task.isFinished())
                    {
                        return getReindexRequiredMessage(user, userKey, i18nTaskKey, time);
                    }
                    else
                    {
                        Date startedTimestamp = task.getStartedTimestamp();
                        if (startedTimestamp != null && startedTimestamp.getTime() < time.getTime())
                        {
                            return getReindexRestartRequiredMessage(user, userKey, i18nTaskKey, time);
                        }
                    }
                }
            }
            if (task != null && task.isStarted() && !task.isFinished())
            {
                final I18nHelper i18n = i18nFactory.getInstance(user);
                return ((IndexTask) task.getTaskContext()).getTaskInProgressMessage(i18n);
            }

            return null;
        }
        finally
        {
            messageLock.unlock();
        }
    }

    private String getRawIndexRequiredMessage(final User user, final String rawMessageKey)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);
        final String rawMessage = i18n.getText(rawMessageKey);
        final String reindexLink = getHelpPathString();
        StringBuilder message = new StringBuilder();
        message.append("<p>").append(i18n.getText("admin.notifications.reindex.rawmessage", new Object[]{rawMessage, reindexLink, "</a>"}));
        message.append("<p>").append(i18n.getText("admin.notifications.reindex.now", "<a href='" + getContextPath() + "/secure/admin/jira/IndexAdmin.jspa'>", "</a>"));
        return message.toString();
    }

    private String getReindexRequiredMessage(User user, String userKey, String i18nTaskKey, Date time)
    {
        return getMessage(user, userKey, i18nTaskKey, time, false);
    }

    private String getMessage(final User user, final String userKey, final String i18nTaskKey, final Date time, boolean restartRequired)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);
        final String restart = restartRequired ? ".restart" : "";
        String timeString = getTimeString(time, i18n);
        String userFullName = userFormatManager.formatUserkey(userKey, FullNameUserFormat.TYPE, "fullName");
        String i18nTask = i18n.getText(i18nTaskKey);

        String reindexLink = getHelpPathString();

        StringBuilder message = new StringBuilder();
        if (userFullName == null)
        {
            message.append("<p>").append(i18n.getText("admin.notifications.task.requires.reindex"+restart+".nouser", new Object[]{i18nTask, timeString, reindexLink, "</a>"}));
        }
        else
        {
            message.append("<p>").append(i18n.getText("admin.notifications.task.requires.reindex"+restart, new Object[]{userFullName, i18nTask, timeString, reindexLink, "</a>"}));
        }
        message.append("<p>").append(i18n.getText("admin.notifications.reindex.now", "<a href='" + getContextPath() + "/secure/admin/jira/IndexAdmin.jspa'>", "</a>"));
        message.append("<p>").append(i18n.getText("admin.notifications.complete.all.changes"));
        return message.toString();
    }

    private String getHelpPathString()
    {
        HelpUtil helpUtil = HelpUtil.getInstance();
        HelpUtil.HelpPath path = helpUtil.getHelpPath("reindex_after_configuration_changes");
        return String.format("<a href=\"%s\" data-helplink=\"online\" target=\"_jirahelp\">", path.getUrl().toString());
    }

    private String getTimeString(final Date time, final I18nHelper i18n)
    {
        final OutlookDate outlookDate = outlookDateManager.getOutlookDate(i18n.getLocale());
        return outlookDate.formatDMYHMS(time);
    }

    private String getReindexRestartRequiredMessage(User user, String userKey, String i18nTaskKey, Date time)
    {
        return getMessage(user, userKey, i18nTaskKey, time, true);
    }

    @Override
    public ReindexMessage getMessageObject()
    {
        messageLock.lock();
        try
        {
            final PropertySet propertySet = propertiesReference.get();
            if (propertySet.exists(PS_KEY_USER))
            {
                final String userKey = propertySet.getString(PS_KEY_USER);
                final String i18nTaskKey = propertySet.getString(PS_KEY_TASK);
                final Date time = propertySet.getDate(PS_KEY_TIME);

                String userName = userKeyService.getUsernameForKey(userKey);
                if (userName == null)
                {
                    userName = userKey;
                }
                return new ReindexMessage(userName, time, i18nTaskKey);
            }
            return null;
        }
        finally
        {
            messageLock.unlock();
        }
    }

    @Override
    public void clearMessageForTimestamp(Date time)
    {
        if (propertiesReference.get().exists(PS_KEY_TIME))
        {
            Date messageTime = propertiesReference.get().getDate(PS_KEY_TIME);
            if (messageTime != null && !time.before(messageTime))
            {
                clear();
            }
        }
    }

    ///CLOVER:OFF
    String getContextPath()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    Date getCurrentDate()
    {
        return new Date(System.currentTimeMillis());
    }
    ///CLOVER:ON

    @SuppressWarnings ("unchecked")
    private TaskDescriptor<Serializable> findActiveIndexTasks()
    {
        return (TaskDescriptor<Serializable>)taskManager.findFirstTask(new TaskMatcher()
        {
            @Override
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return !descriptor.isFinished() && descriptor.getTaskContext() instanceof IndexTask;
            }
        });
    }
}
