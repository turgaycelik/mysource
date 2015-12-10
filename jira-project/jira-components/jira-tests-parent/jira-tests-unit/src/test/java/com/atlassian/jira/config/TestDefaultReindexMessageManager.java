package com.atlassian.jira.config;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.web.util.MockOutlookDate;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.MockTaskDescriptor;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.mock.propertyset.MockPropertySet;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultReindexMessageManager
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    @Mock
    private JiraPropertySetFactory jiraPropertySetFactory;

    @Mock
    private UserFormatManager userFormatManager;

    private UserKeyService userKeyService = new MockUserKeyService();

    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    @Mock
    private OutlookDateManager outlookDateManager;

    private MockPropertySet propertySet = new MockPropertySet();

    @Mock
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private User user = null;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private TaskManager taskManager;

    private CacheManager cacheManager;
    private SimpleClusterLockService clusterLockService;

    @Before
    public void setUp() throws Exception
    {
        userKeyService = new MockUserKeyService();
        cacheManager = new MemoryCacheManager();
        clusterLockService = new SimpleClusterLockService();

        when(jiraPropertySetFactory.buildCachingDefaultPropertySet(DefaultReindexMessageManager.PS_KEY, true))
                .thenReturn(propertySet);
    }

    @Test
    public void testPushMessageNullUser() throws Exception
    {
        final Date currentDate = new Date(System.currentTimeMillis());

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            Date getCurrentDate()
            {
                return currentDate;
            }
        };
        manager.start();
        manager.pushMessage(user, "i18n");

        assertThat(propertySet.getString("user"), equalTo(""));
        assertThat(propertySet.getDate("time"), equalTo(currentDate));
        assertThat(propertySet.getString("task"), equalTo("i18n"));
    }

    @Test
    public void testPushMessageNonNullUser() throws Exception
    {
        final Date currentDate = new Date(System.currentTimeMillis());
        user = new MockUser("bill");

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            Date getCurrentDate()
            {
                return currentDate;
            }
        };

        manager.start();
        manager.pushMessage(user, "i18n");
        assertThat(propertySet.getString("user"), equalTo("bill"));
        assertThat(propertySet.getDate("time"), equalTo(currentDate));
        assertThat(propertySet.getString("task"), equalTo("i18n"));

    }

    @Test
    public void testClearNoExistingMessage() throws Exception
    {
        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService);
        manager.start();
        manager.clear();
    }

    @Test
    public void testClearExistingMessage() throws Exception
    {
        propertySet.setString("user", "admin");
        propertySet.setDate("time", new Date());
        propertySet.setString("task", "task");
        propertySet.setString("rawmsg", "msg");

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService);

        manager.start();
        manager.clear();

        assertThat(propertySet.exists("user"), equalTo(false));
        assertThat(propertySet.exists("time"), equalTo(false));
        assertThat(propertySet.exists("task"), equalTo(false));
        assertThat(propertySet.exists("rawmsg"), equalTo(false));
    }

    @Test
    public void testGetMessageNoExistingMessage() throws Exception
    {
        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService);

        manager.start();
        assertNull(manager.getMessage(user));
    }

    @Test
    public void testGetMessage() throws Exception
    {
        Date date = new Date(1248402833700L);
        propertySet.setString("user", "bill");
        propertySet.setString("task", "admin.bla.bla");
        propertySet.setDate("time", date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        final MockOutlookDate mockOutlookDate = mockOutlookDate();
        when(outlookDateManager.getOutlookDate(Locale.getDefault())).thenReturn(mockOutlookDate);
        when(userFormatManager.formatUserkey("bill", FullNameUserFormat.TYPE, "fullName")).thenReturn("Full Name of Bill");
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        manager.start();
        final String result = manager.getMessage(user);
        assertThat(result, Matchers.containsString("admin.notifications.task.requires.reindex"));
        assertThat(result, Matchers.containsString("Full Name of Bill"));
        assertThat(result, Matchers.containsString("admin.bla.bla"));
        assertThat(result, Matchers.containsString("THE DATE"));
    }

    @Test
    public void testGetRawMessage() throws Exception
    {
        Date date = new Date(1248402833700L);
        propertySet.setString("user", "bill");
        propertySet.setString("task", null);
        propertySet.setDate("time", date);
        propertySet.setString("rawmsg", "admin.blah");

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        manager.start();
        final String result = manager.getMessage(user);
        assertThat(result, Matchers.containsString("admin.notifications.reindex.rawmessage"));
        assertThat(result, Matchers.containsString("admin.blah"));
    }

    @Test
    public void testGetMessageNullUser() throws Exception
    {
        Date date = new Date(1248402833700L);

        propertySet.setString("user", "not-existing");
        propertySet.setString("task", "admin.bla.bla");
        propertySet.setDate("time", date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);

        final MockOutlookDate mockOutlookDate = mockOutlookDate();
        when(outlookDateManager.getOutlookDate(Locale.getDefault())).thenReturn(mockOutlookDate);
        when(userFormatManager.formatUserkey("not-existing", FullNameUserFormat.TYPE, "fullName")).thenReturn(null);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };

        manager.start();
        final String result = manager.getMessage(user);
        assertThat(result, Matchers.containsString("admin.notifications.task.requires.reindex.nouser"));
        assertThat(result, Matchers.containsString("admin.notifications.reindex.now"));
        assertThat(result, Matchers.containsString("admin.bla.bla"));
        assertThat(result, Matchers.containsString("THE DATE"));
    }

    @Test
    public void testGetMessageNonAdmin() throws Exception
    {
        user = new MockUser("user");
        propertySet.setString("user", user.getName());

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService);

        manager.start();
        assertNull(manager.getMessage(user));
    }

    @Test
    public void testGetMessageIndexInProgress() throws Exception
    {
        final Date date = new Date();
        propertySet.setString("user", "bill");
        propertySet.setString("task", "admin.bla.bla");
        propertySet.setDate("time", date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        MockTaskDescriptor<?> task = new MyMockTask(new Date(date.getTime() + 1000), true, false);
        task.setTaskContext(new MyIndexTask());
        //noinspection unchecked
        when(taskManager.findFirstTask(Mockito.any(TaskMatcher.class))).thenReturn((TaskDescriptor) task);

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        manager.start();
        final String result = manager.getMessage(user);
        assertEquals("admin.notifications.reindex.in.progress.foreground", result);
    }

    @Test
    public void testGetMessageTaskFinished() throws Exception
    {
        final Date date = new Date();
        propertySet.setString("user", "bill");
        propertySet.setString("task", "admin.bla.bla");
        propertySet.setDate("time", date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(outlookDateManager.getOutlookDate(Locale.getDefault())).thenReturn(mockOutlookDate());
        when(userFormatManager.formatUserkey("bill", FullNameUserFormat.TYPE, "fullName")).thenReturn(null);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        MockTaskDescriptor<?> task = new MyMockTask(new Date(date.getTime() + 1000), true, true);
        //noinspection unchecked
        when(taskManager.findFirstTask(Mockito.any(TaskMatcher.class))).thenReturn((TaskDescriptor)task);
        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        manager.start();
        final String result = manager.getMessage(user);
        assertThat(result, Matchers.containsString("admin.notifications.task.requires.reindex.nouser"));
        assertThat(result, Matchers.containsString("admin.bla.bla"));
        assertThat(result, Matchers.containsString("THE DATE"));
    }

    @Test
    public void testGetMessageReindexRestartRequired() throws Exception
    {
        final Date date = new Date();
        propertySet.setString("user", "bill");
        propertySet.setString("task", "admin.bla.bla");
        propertySet.setDate("time", date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(outlookDateManager.getOutlookDate(Locale.getDefault())).thenReturn(mockOutlookDate());
        when(userFormatManager.formatUserkey("bill", FullNameUserFormat.TYPE, "fullName")).thenReturn("Full Name of Bill");
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        MockTaskDescriptor<?> task = new MyMockTask(new Date(date.getTime() - 1000), true, false);
        //noinspection unchecked
        when(taskManager.findFirstTask(Mockito.any(TaskMatcher.class))).thenReturn((TaskDescriptor) task);

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userKeyService,
                userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory,
                permissionManager, taskManager, cacheManager, clusterLockService)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        manager.start();
        final String result = manager.getMessage(user);
        assertThat(result, Matchers.containsString("admin.notifications.task.requires.reindex.restart"));
        assertThat(result, Matchers.containsString("Full Name of Bill"));
        assertThat(result, Matchers.containsString("admin.bla.bla"));
        assertThat(result, Matchers.containsString("THE DATE"));
    }

    private MockOutlookDate mockOutlookDate()
    {
        return new MockOutlookDate(Locale.getDefault())
        {
            @Override
            public String formatDMYHMS(final Date date)
            {
                return "THE DATE";
            }
        };
    }

    private static class MyMockTask extends MockTaskDescriptor<Serializable>
    {
        private final Date date;
        private final boolean started;
        private final boolean finished;

        public MyMockTask(Date date, boolean started, boolean finished)
        {
            this.date = date;
            this.started = started;
            this.finished = finished;
        }

        @Override
        public boolean isStarted()
        {
            return started;
        }

        @Override
        public boolean isFinished()
        {
            return finished;
        }

        @Override
        public Date getStartedTimestamp()
        {
            return date;
        }
    }

    private static class MyIndexTask implements IndexTask
    {
        @Override
        public String getTaskInProgressMessage(final I18nHelper i18n)
        {
            return "admin.notifications.reindex.in.progress.foreground";
        }

        @Override
        public String buildProgressURL(final Long taskId)
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
