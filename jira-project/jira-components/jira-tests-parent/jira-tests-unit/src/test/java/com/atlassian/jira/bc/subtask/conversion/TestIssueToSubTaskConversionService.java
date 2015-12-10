package com.atlassian.jira.bc.subtask.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManagerImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestIssueToSubTaskConversionService
{
    private JiraServiceContext ctx;
    private ApplicationUser testUser;

    private final Issue subTask = new MockIssue(new Long(567))
    {

        @Override
        public String getKey()
        {
            return "abc";
        }

        @Override
        public boolean isSubTask()
        {
            return true;
        }

        @Override
        public Collection getSubTaskObjects()
        {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Project getProjectObject()
        {
            return new MockProject(new Long(999));

        }
    };
    private final Issue normalIssue = new MockIssue(new Long(222))
    {
        @Override
        public String getKey()
        {
            return "xyz";
        }

        @Override
        public Collection getSubTaskObjects()
        {
            return Collections.EMPTY_LIST;
        }

        @Override
        public boolean isSubTask()
        {
            return false;
        }

        @Override
        public Project getProjectObject()
        {
            return new MockProject(new Long(999));

        }
    };
    private final Issue normalIssueWithSubtasks = new MockIssue(new Long(100))
    {

        @Override
        public String getKey()
        {
            return "stu";
        }

        @Override
        public Collection getSubTaskObjects()
        {
            final List subs = new ArrayList();
            subs.add(subTask);
            return subs;
        }

        @Override
        public boolean isSubTask()
        {
            return false;
        }

        @Override
        public Project getProjectObject()
        {
            return new MockProject(new Long(999));

        }
    };

    private PermissionManager posPermMgr;
    private PermissionManager negPermMgr;

    private final SubTaskManager enabledSubTasks = new MockSubTaskManager();

    private final EventPublisher eventPublisher = new MockEventPublisher();

    @Mock
    private IssueType mockSubTaskIssueType;
    @Mock
    private IssueType mockIssueType;
    @Mock
    private SubTaskManager mockSubTaskManager;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;

    @Rule public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    private IssueTypeSchemeManager listWithSubtasks;
    private IssueTypeSchemeManager listWithoutSubtasks;

    @Before
    public void setUp() throws Exception
    {
        testUser = new MockApplicationUser("Tester");
        ctx = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        negPermMgr = new MockPermissionManager(false);
        posPermMgr = new MockPermissionManager(true);
        when(mockSubTaskIssueType.isSubTask()).thenReturn(Boolean.TRUE);
        when(mockSubTaskIssueType.getName()).thenReturn("SubTaskType");
        when(mockSubTaskIssueType.getId()).thenReturn("1111");

        when(mockIssueType.isSubTask()).thenReturn(Boolean.FALSE);
        when(mockIssueType.getName()).thenReturn("IssueType");
        when(mockIssueType.getId()).thenReturn("9999");

        listWithSubtasks = new IssueTypeSchemeManagerImpl(null, null, null, null, eventPublisher)
        {
            @Override
            public Collection getIssueTypesForProject(final Project project)
            {
                final List issueTypes = new ArrayList();
                issueTypes.add(mockIssueType);
                issueTypes.add(mockSubTaskIssueType);
                issueTypes.add(mockIssueType);

                return issueTypes;
            }
        };

        listWithoutSubtasks = new IssueTypeSchemeManagerImpl(null, null, null, null, eventPublisher)
        {
            @Override
            public Collection getIssueTypesForProject(final Project project)
            {
                final List issueTypes = new ArrayList();
                issueTypes.add(mockIssueType);
                issueTypes.add(mockIssueType);
                issueTypes.add(mockIssueType);

                return issueTypes;
            }
        };

    }

    public void tearDown() throws Exception
    {
        listWithSubtasks = null;
        listWithoutSubtasks = null;
        mockIssueType = null;
        mockSubTaskIssueType = null;
    }

    @Test
    public void testCanConvertIssue()
    {

        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, null, null);

        // test for enterprise edition
        assertTrue(convService.canConvertIssue(ctx, normalIssue));
    }

    @Test
    public void testCannotConvertSubTask()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        assertFalse(convService.canConvertIssue(ctx, subTask));
        assertErrorMessagePresent(ctx, "convert.issue.to.subtask.errormessage.subtaskalreadyabc");
    }

    @Test
    public void testCannotConvertIssueWithSubTasks()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        assertFalse(convService.canConvertIssue(ctx, normalIssueWithSubtasks));
        assertErrorMessagePresent(ctx, "convert.issue.to.subtask.errormessage.issuehassubtasksstu");
    }

    @Test
    public void testCannotConvertIssueWhenSubTasksDisabled()
    {
        final MockSubTaskManager disabledSubTasks = new MockSubTaskManager();
        disabledSubTasks.disableSubTasks();
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, disabledSubTasks, listWithSubtasks,
            null);
        assertFalse(convService.canConvertIssue(ctx, normalIssue));
        assertErrorMessagePresent(ctx, "convert.issue.to.subtask.errormessage.subtasksdisabled");
    }

    @Test
    public void testCannotConvertIssueNoPermission()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(negPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        assertFalse(convService.canConvertIssue(ctx, normalIssue));
        assertErrorMessagePresent(ctx, "convert.issue.to.subtask.errormessage.nopermisionuser");
    }

    @Test
    public void testCannotConvertIssueNoPermissionAnon()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(negPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext newCtx = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
        assertFalse(convService.canConvertIssue(newCtx, normalIssue));
        assertErrorMessagePresent(newCtx, "convert.issue.to.subtask.errormessage.nopermissionanon");
    }

    @Test
    public void testCannotConvertIssueInProjectNoSubtasks()
    {
        final IssueToSubTaskConversionService convService = new DefaultIssueToSubTaskConversionService(posPermMgr, enabledSubTasks,
            listWithoutSubtasks, null, null, null, null, null, null)
        {
            @Override
            protected String getText(final String key)
            {
                return key;
            }

            @Override
            protected String getText(final String key, final Object param)
            {
                return key + param;
            }

            @Override
            protected boolean projectHasSubTasks(final Project project)
            {
                return false;
            }
        };
        assertFalse(convService.canConvertIssue(ctx, normalIssue));
        assertErrorMessagePresent(ctx, "convert.issue.to.subtask.errormessage.nosubtaskissuetypesforprojectnull");
    }

    @Test
    public void testCanConvertNulls()
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(Boolean.TRUE);

        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(null, mockSubTaskManager, listWithSubtasks, null);
        try
        {
            convService.canConvertIssue(null, null);
            fail("Null Issue is not allowed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            convService.canConvertIssue(ctx, null);
            fail("Null Issue is not allowed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            convService.canConvertIssue(null, normalIssue);
            fail("Null Issue is not allowed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testHasPermission()
    {
        final AtomicBoolean isPermissionCorrect = new AtomicBoolean(false);
        final AtomicBoolean isIssueCorrect = new AtomicBoolean(false);
        final AtomicBoolean isUserCorrect = new AtomicBoolean(false);

        final PermissionManager permissionManager = new DefaultPermissionManager(null)
        {
            @Override
            public boolean hasPermission(final int permissionsId, final Issue issue, final ApplicationUser u)
            {
                isPermissionCorrect.set(Permissions.EDIT_ISSUE == permissionsId);
                isIssueCorrect.set(normalIssue.equals(issue));
                isUserCorrect.set(u.equals(testUser));

                return true;
            }
        };

        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(permissionManager, enabledSubTasks,
            listWithSubtasks, null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.hasPermission(context, normalIssue);

        // verify that call thru with correct parameters
        assertTrue(isPermissionCorrect.get());
        assertTrue(isIssueCorrect.get());
        assertTrue(isUserCorrect.get());
    }

    @Test
    public void testValidateParentIssueHappy()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateParentIssue(context, normalIssue, normalIssueWithSubtasks, "field");
        assertFalse(context.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testInvalidParentIssueSameIssue()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateParentIssue(context, normalIssue, normalIssue, "field");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.parentsameissuexyz", context.getErrorCollection().getErrors().get("field"));
    }

    @Test
    public void testInvalidParentIssueSubTask()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateParentIssue(context, normalIssue, subTask, "field");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.parentissubtaskabc", context.getErrorCollection().getErrors().get("field"));
    }

    public void testInvalidParentIssueNoPerm()
    {
        // No permission to
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(negPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateParentIssue(context, normalIssue, normalIssueWithSubtasks, "field");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.invalidparentissuekeystu", context.getErrorCollection().getErrors().get("field"));
    }

    @Test
    public void testInvalidParentIssueDiffProject()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        final Issue issueDiffProject = new MockIssue(new Long(999))
        {

            @Override
            public Project getProjectObject()
            {
                return new MockProject(new Long(666));

            }

            @Override
            public String getKey()
            {
                return "stu";
            }

            @Override
            public Collection getSubTaskObjects()
            {
                final List subs = new ArrayList();
                subs.add(subTask);
                return subs;
            }

            @Override
            public boolean isSubTask()
            {
                return false;
            }
        };

        convService.validateParentIssue(context, normalIssue, issueDiffProject, "field");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.differentprojectstuxyz", context.getErrorCollection().getErrors().get("field"));
    }

    @Test
    public void testValidateIssueTypeHappy()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateTargetIssueType(context, normalIssue, mockSubTaskIssueType, "field");
        assertFalse(context.getErrorCollection().hasAnyErrors());

    }

    @Test
    public void testValidateIssueTypeNotSubTask()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks, listWithSubtasks,
            null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateTargetIssueType(context, normalIssue, mockIssueType, "field");
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.issuetypenotsubtaskIssueType", context.getErrorCollection().getErrors().get("field"));

    }

    @Test
    public void testValidateIssueTypeNotInProject()
    {
        final IssueToSubTaskConversionService convService = createNewIssueToSubTaskConversionService(posPermMgr, enabledSubTasks,
            listWithoutSubtasks, null);
        final JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        convService.validateTargetIssueType(context, normalIssue, mockSubTaskIssueType, "field");
        assertTrue(context.getErrorCollection().getErrorMessages().toString(), context.getErrorCollection().hasAnyErrors());
        assertEquals(1, context.getErrorCollection().getErrors().size());
        assertEquals("convert.issue.to.subtask.error.issuetypenotforprojectSubTaskType", context.getErrorCollection().getErrors().get("field"));

    }

    private static class MockPermissionManager extends DefaultPermissionManager
    {

        private final boolean hasPermission;

        public MockPermissionManager(final boolean hasPermission)
        {
            super(null);
            this.hasPermission = hasPermission;
        }

        @Override
        public boolean hasPermission(final int permissionsId, final Issue issue, final ApplicationUser user)
        {
            return hasPermission;
        }
    }

    private IssueToSubTaskConversionService createNewIssueToSubTaskConversionService(final PermissionManager permissionManager, final SubTaskManager subtaskManager, final IssueTypeSchemeManager issueTypeSchemeManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        return new DefaultIssueToSubTaskConversionService(permissionManager, subtaskManager, issueTypeSchemeManager, jiraAuthenticationContext, null,
            null, null, null, null)
        {

            @Override
            protected String getText(final String key)
            {
                return key;
            }

            @Override
            protected String getText(final String key, final Object param)
            {
                return key + param;
            }

            @Override
            protected String getText(final String key, final String param0, final String param1)
            {
                return key + param0 + param1;
            }

            @Override
            protected boolean projectHasSubTasks(final Project project)
            {
                return true;
            }

        };
    }

    private void assertErrorMessagePresent(final JiraServiceContext ctx, final String messageKey)
    {
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(messageKey, ctx.getErrorCollection().getErrorMessages().iterator().next());
    }

    public void testIsNotNullAndNotEqualTo()
    {
        assertFalse(DefaultIssueToSubTaskConversionService.isNotNullAndNotEqualTo(null, null));
        assertFalse(DefaultIssueToSubTaskConversionService.isNotNullAndNotEqualTo(null, new Long(1)));
        assertTrue(DefaultIssueToSubTaskConversionService.isNotNullAndNotEqualTo(new Long(1), null));
        assertFalse(DefaultIssueToSubTaskConversionService.isNotNullAndNotEqualTo(new Long(1), new Long(1)));
        assertTrue(DefaultIssueToSubTaskConversionService.isNotNullAndNotEqualTo(new Long(1), new Long(2)));
    }

    public void testgetLongToStringNullSafe()
    {
        assertNull(DefaultIssueToSubTaskConversionService.getLongToStringNullSafe(null));
        assertEquals("1", DefaultIssueToSubTaskConversionService.getLongToStringNullSafe(new Long(1)));
        assertEquals("123", DefaultIssueToSubTaskConversionService.getLongToStringNullSafe(new Long(123)));
    }

}
