package com.atlassian.jira.bc.group;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.apache.commons.collections.IteratorUtils;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.12 */
public class TestDefaultGroupService
{
    // if you don't pass a mock user to JiraServiceContext then you'll usually find that eventually someone, somewhere
    // calls get default locale from the Component Manager which brings up the entire world.
    final ApplicationUser mockUser = new MockApplicationUser("test");
    private final MockUserManager mockUserManager = new MockUserManager();

    @Before
    public void setUp()
    {
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(mockComponentWorker);
        mockComponentWorker.registerMock(UserManager.class, mockUserManager);
    }

    @Test
    public void testGetCommentsAndWorklogsGuardedByGroupCount()
    {
        final Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("getCountForCommentsRestrictedByGroup", P.ANY_ARGS, new Long(1));
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getCountForWorklogsRestrictedByGroup", P.ANY_ARGS, new Long(1));

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, (CommentManager) mockCommentManager.proxy(),
            (WorklogManager) mockWorklogManager.proxy(), null, null, null, null, null, null, null, null, null, null);

        assertEquals(2, defaultGroupService.getCommentsAndWorklogsGuardedByGroupCount("test group"));
        mockCommentManager.verify();
        mockWorklogManager.verify();
    }

    @Test
    public void testGetCommentsAndWorklogsGuardedByGroupNullGroupName()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
        try
        {
            defaultGroupService.getCommentsAndWorklogsGuardedByGroupCount(null);
            fail();
        }
        catch (final Exception e)
        {
            assertEquals("The provided group name must not be null.", e.getMessage());
        }
    }

    @Test
    public void testAreOnlyGroupsGrantingUserAdminPermissionsSysAdminRemovingSelf()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (ApplicationUser) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(true);
        mockGlobalPermissionManagerControl.replay();

        final MockControl mockGlobalPermGroupAssUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermGroupAssUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermGroupAssUtilControl.getMock();
        mockGlobalPermGroupAssUtil.isRemovingAllMySysAdminGroups(EasyList.build("TestGroup"), mockUser);
        mockGlobalPermGroupAssUtilControl.setReturnValue(true);

        mockGlobalPermGroupAssUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, mockGlobalPermGroupAssUtil, null,
            null, null, null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertTrue(defaultGroupService.areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, EasyList.build("TestGroup")));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals(
            "You cannot delete a group that grants you system administration privileges if no other group exists that also grants you system administration privileges.",
            errorCollection.getErrorMessages().iterator().next());
        mockGlobalPermissionManagerControl.verify();
        mockGlobalPermGroupAssUtilControl.verify();
    }

    private JiraServiceContextImpl getContext(final ErrorCollection errorCollection)
    {
        return new JiraServiceContextImpl(mockUser, errorCollection)
        {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
    }

    @Test
    public void testAreOnlyGroupsGrantingUserAdminPermissionsSysAdminNotRemovingSelf()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (ApplicationUser) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(true);
        mockGlobalPermissionManagerControl.replay();

        final MockControl mockGlobalPermGroupAssUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermGroupAssUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermGroupAssUtilControl.getMock();
        mockGlobalPermGroupAssUtil.isRemovingAllMySysAdminGroups(EasyList.build("TestGroup"), mockUser);
        mockGlobalPermGroupAssUtilControl.setReturnValue(false);

        mockGlobalPermGroupAssUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, mockGlobalPermGroupAssUtil, null,
            null, null, null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, EasyList.build("TestGroup")));
        assertEquals(0, errorCollection.getErrorMessages().size());
        mockGlobalPermissionManagerControl.verify();
        mockGlobalPermGroupAssUtilControl.verify();
    }

    @Test
    public void testAreOnlyGroupsGrantingUserAdminPermissionsAdminRemovingSelf()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (ApplicationUser) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(false);
        mockGlobalPermissionManagerControl.replay();

        final MockControl mockGlobalPermGroupAssUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermGroupAssUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermGroupAssUtilControl.getMock();
        mockGlobalPermGroupAssUtil.isRemovingAllMyAdminGroups(EasyList.build("TestGroup"), mockUser);
        mockGlobalPermGroupAssUtilControl.setReturnValue(true);

        mockGlobalPermGroupAssUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, mockGlobalPermGroupAssUtil, null,
            null, null, null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertTrue(defaultGroupService.areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, EasyList.build("TestGroup")));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals(
            "You cannot delete a group that grants you administration privileges if no other group exists that also grants you administration privileges.",
            errorCollection.getErrorMessages().iterator().next());
        mockGlobalPermissionManagerControl.verify();
        mockGlobalPermGroupAssUtilControl.verify();
    }

    @Test
    public void testAreOnlyGroupsGrantingUserAdminPermissionsAdminNotRemovingSelf()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (ApplicationUser) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(false);
        mockGlobalPermissionManagerControl.replay();

        final MockControl mockGlobalPermGroupAssUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermGroupAssUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermGroupAssUtilControl.getMock();
        mockGlobalPermGroupAssUtil.isRemovingAllMyAdminGroups(EasyList.build("TestGroup"), mockUser);
        mockGlobalPermGroupAssUtilControl.setReturnValue(false);

        mockGlobalPermGroupAssUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, mockGlobalPermGroupAssUtil, null,
            null, null, null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, EasyList.build("TestGroup")));
        assertEquals(0, errorCollection.getErrorMessages().size());
        mockGlobalPermissionManagerControl.verify();
        mockGlobalPermGroupAssUtilControl.verify();
    }

    @Test
    public void testIsAdminDeletingSysAdminGroupAdminNotDeletingGroup()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(false);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(EasyList.build("AnotherGroup"));
        mockGlobalPermissionManagerControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, null, null, null, null,
            null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.isAdminDeletingSysAdminGroup(jiraServiceContext, "TestGroup"));
        assertEquals(0, errorCollection.getErrorMessages().size());
        mockGlobalPermissionManagerControl.verify();
    }

    @Test
    public void testIsAdminDeletingSysAdminGroupAdminDeletingGroup()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(false);
        mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(EasyList.build("TestGroup", "AnotherGroup"));
        mockGlobalPermissionManagerControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, null, null, null, null,
            null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertTrue(defaultGroupService.isAdminDeletingSysAdminGroup(jiraServiceContext, "TestGroup"));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals(
            "Cannot delete group, only System Administrators can delete groups associated with the System Administrators global permission.",
            errorCollection.getErrorMessages().iterator().next());
        mockGlobalPermissionManagerControl.verify();
    }

    @Test
    public void testIsAdminDeletingSysAdminGroupSysAdminDeletingGroup()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (com.atlassian.crowd.embedded.api.User) null);
        mockGlobalPermissionManagerControl.setDefaultReturnValue(true);
        mockGlobalPermissionManagerControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, null, null, null, null,
            null, null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.isAdminDeletingSysAdminGroup(jiraServiceContext, "TestGroup"));
        assertEquals(0, errorCollection.getErrorMessages().size());
        mockGlobalPermissionManagerControl.verify();
    }

    @Test
    public void testUpdateCommentsAndWorklogs()
    {
        final Mock mockCommentManager = new Mock(CommentManager.class);
        mockCommentManager.expectAndReturn("swapCommentGroupRestriction", P.args(P.eq("TestGroup"), P.eq("SwapGroup")), 2);
        mockCommentManager.setStrict(true);

        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("swapWorklogGroupRestriction", P.args(P.eq("TestGroup"), P.eq("SwapGroup")), 2);
        mockWorklogManager.setStrict(true);

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, (CommentManager) mockCommentManager.proxy(),
            (WorklogManager) mockWorklogManager.proxy(), null, null, null, null, null, null, null, null, null, null);

        defaultGroupService.updateCommentsAndWorklogs(null, "TestGroup", "SwapGroup");

        mockCommentManager.verify();
        mockWorklogManager.verify();
    }

    @Test
    public void testValidateDeleteHappyPath()
    {
        final AtomicBoolean isGroupNullCalled = new AtomicBoolean(false);
        final AtomicBoolean isOnlyCalled = new AtomicBoolean(false);
        final AtomicBoolean isAdminCalled = new AtomicBoolean(false);
        final AtomicBoolean getComments = new AtomicBoolean(false);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            boolean isGroupNull(final String groupName)
            {
                isGroupNullCalled.set(true);
                return false;
            }

            @Override
            public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
            {
                isOnlyCalled.set(true);
                return false;
            }

            @Override
            public boolean isAdminDeletingSysAdminGroup(final JiraServiceContext jiraServiceContext, final String groupName)
            {
                isAdminCalled.set(true);
                return false;
            }

            @Override
            public long getCommentsAndWorklogsGuardedByGroupCount(final String groupName)
            {
                getComments.set(true);
                return 0;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertTrue(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "SwapGroup"));

        assertFalse(errorCollection.hasAnyErrors());
        assertTrue(isGroupNullCalled.get());
        assertTrue(isOnlyCalled.get());
        assertTrue(isAdminCalled.get());
        assertTrue(getComments.get());
    }

    @Test
    public void testValidateDeleteExternalUserManagementEnabled()
    {
        mockUserManager.setGroupWritableDirectory(false);
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, (JiraContactHelper) mockJiraContactHelper.proxy(), null)
        {
            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "SwapGroup"));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Cannot delete group, as all directories are read-only.",
            errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteInvalidGroupName()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, null, "SwapGroup"));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The group 'null' is not a valid group.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteIsOnlyGroupGrantingUserAdminPermissions()
    {

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
            {
                return true;
            }

            @Override
            boolean isGroupNull(final String groupName)
            {
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "SwapGroup"));
    }

    @Test
    public void testValidateDeleteIsAdminDeletingSysAdminGroup()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
            {
                return false;
            }

            @Override
            public boolean isAdminDeletingSysAdminGroup(final JiraServiceContext jiraServiceContext, final String groupName)
            {
                return true;
            }

            @Override
            boolean isGroupNull(final String groupName)
            {
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "SwapGroup"));
    }

    @Test
    public void testValidateDeleteCommentsExistAndNoSwapGroup()
    {

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {

            @Override
            public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
            {
                return false;
            }

            @Override
            public boolean isAdminDeletingSysAdminGroup(final JiraServiceContext jiraServiceContext, final String groupName)
            {
                return false;
            }

            @Override
            public long getCommentsAndWorklogsGuardedByGroupCount(final String groupName)
            {
                return 1;
            }

            @Override
            boolean isGroupNull(final String groupName)
            {
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", null));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You must specify a group to move comments/worklogs to.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteSwapGroupSameAsDeleteGroup()
    {

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null,
            null, null, null, null, null, null, null, null, null, null, null, null, null)
        {

            @Override
            public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
            {
                return false;
            }

            @Override
            public boolean isAdminDeletingSysAdminGroup(final JiraServiceContext jiraServiceContext, final String groupName)
            {
                return false;
            }

            @Override
            public long getCommentsAndWorklogsGuardedByGroupCount(final String groupName)
            {
                return 1;
            }

            @Override
            boolean isGroupNull(final String groupName)
            {
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);
        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "TestGroup"));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You cannot swap comments/worklogs to the group you are deleting.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testDeleteHappyPath()
    {
        final Mock mockProjectRoleService = new Mock(ProjectRoleService.class);
        mockProjectRoleService.setStrict(true);
        mockProjectRoleService.expectVoid("removeAllRoleActorsByNameAndType", P.ANY_ARGS);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.ADMINISTER), P.IS_ANYTHING), Boolean.TRUE);
        mockPermissionManager.expectVoid("removeGroupPermissions", P.args(P.eq("TestGroup")));

        final Mock mockNotificationManager = new Mock(NotificationSchemeManager.class);
        mockNotificationManager.setStrict(true);
        mockNotificationManager.expectAndReturn("removeEntities", P.args(P.eq(GroupDropdown.DESC), P.eq("TestGroup")), Boolean.TRUE);

        final Mock mockSubscriptionManager = new Mock(SubscriptionManager.class);
        mockSubscriptionManager.setStrict(true);
        mockSubscriptionManager.expectVoid("deleteSubscriptionsForGroup", P.ANY_ARGS);

        final AtomicBoolean calledSharePermissionDeleteUtils = new AtomicBoolean(false);
        final SharePermissionDeleteUtils deleteUtils = new SharePermissionDeleteUtils(null)
        {
            @Override
            public void deleteGroupPermissions(final String groupName)
            {
                calledSharePermissionDeleteUtils.set(true);
            }
        };

        final AtomicBoolean updateCommentAndGroupsCalled = new AtomicBoolean(false);
        final AtomicBoolean removeGroupCalled = new AtomicBoolean(false);
        final AtomicBoolean clearCalled = new AtomicBoolean(false);

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null,
            (NotificationSchemeManager) mockNotificationManager.proxy(), (PermissionManager) mockPermissionManager.proxy(),
            (ProjectRoleService) mockProjectRoleService.proxy(), null, null, deleteUtils, (SubscriptionManager) mockSubscriptionManager.proxy(), null, null, null)
        {
            @Override
            void updateCommentsAndWorklogs(final User user, final String groupName, final String swapGroup)
            {
                updateCommentAndGroupsCalled.set(true);
            }

            @Override
            Group getGroup(final String groupName)
            {
                return null;
            }

            @Override
            void removeGroup(final Group group) throws PermissionException
            {
                removeGroupCalled.set(true);
            }

            @Override
            void clearIssueSecurityLevelCache()
            {
                clearCalled.set(true);
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertTrue(defaultGroupService.delete(jiraServiceContext, "TestGroup", "SwapGroup"));

        assertTrue(updateCommentAndGroupsCalled.get());
        assertTrue(clearCalled.get());
        assertTrue(removeGroupCalled.get());
        assertTrue(calledSharePermissionDeleteUtils.get());
        mockPermissionManager.verify();
        mockProjectRoleService.verify();
        mockNotificationManager.verify();
        mockSubscriptionManager.verify();
    }

    @Test
    public void testDeleteNoAdminPerm()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.ADMINISTER), P.IS_ANYTHING), Boolean.FALSE);

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null,
            (PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.delete(jiraServiceContext, "TestGroup", "SwapGroup"));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errorCollection.getErrorMessages().iterator().next());
        mockPermissionManager.verify();
    }

    @Test
    public void testValidateDeleteNoAdminPerm()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.ADMINISTER), P.IS_ANYTHING), Boolean.FALSE);

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null,
            (PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errorCollection);

        assertFalse(defaultGroupService.validateDelete(jiraServiceContext, "TestGroup", "SwapGroup"));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errorCollection.getErrorMessages().iterator().next());
        mockPermissionManager.verify();
    }

    @Test
    public void testClearIssueSecurityLevelCacheEnterprise()
    {
        final Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        mockIssueSecurityLevelManager.setStrict(true);
        mockIssueSecurityLevelManager.expectVoid("clearUsersLevels");
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null,
            (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), null, null, null, null, null, null);

        defaultGroupService.clearIssueSecurityLevelCache();
        mockIssueSecurityLevelManager.verify();
    }

    @Test
    public void testValidateGroupNamesExistHappy()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            boolean isGroupNull(final String groupName)
            {
                return false;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        assertTrue(defaultGroupService.validateGroupNamesExist(EasyList.build("TestGroup", "AnotherGroup"), errors, null));
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testValidateGroupNamesExistFailPath()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            boolean isGroupNull(final String groupName)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        assertFalse(defaultGroupService.validateGroupNamesExist(EasyList.build("TestGroup"), errors, new MockI18nBean()));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        final String msg = "The group 'TestGroup' is not a valid group.";
        assertEquals(msg, errors.getErrorMessages().iterator().next());

    }

    /** Tests we pass through to the bulk method. */
    @Test
    public void testValidateAddUsertoGroup()
    {
        final AtomicBoolean bulkWasCalled = new AtomicBoolean(false);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            public BulkEditGroupValidationResult validateAddUsersToGroup(final JiraServiceContext jiraServiceContext, final Collection groupsToJoin, final Collection userNames)
            {
                bulkWasCalled.set(true);
                return new BulkEditGroupValidationResult(true);
            }
        };
        assertTrue(defaultGroupService.validateAddUserToGroup(null, null, null));
        assertTrue(bulkWasCalled.get());
    }

    @Test
    public void testValidateAddUsersToGroupNullServiceContext()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
        try
        {
            defaultGroupService.validateAddUsersToGroup(null, null, null);
            fail("expected runtime exception");
        }
        catch (final RuntimeException yay)
        {}
    }

    @Test
    public void testValidateAddUsersToGroupNullGroupsToJoin()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        try
        {
            defaultGroupService.validateAddUsersToGroup(jiraServiceContext, null, null);
            fail("expected RuntimeException");
        }
        catch (final RuntimeException yay)
        {}
    }

    @Test
    public void testValidateAddUsersToGroupNonExistentGroups()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                errorCollection.addErrorMessage("non group");
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), null).isSuccess());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("non group", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAddUsersToGroupNullUser()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return true;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), EasyList.build((String) null)).isSuccess());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot add user. 'null' does not exist", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAddUsersToGroupExternalUserManagement()
    {
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, (JiraContactHelper) mockJiraContactHelper.proxy(), null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), null).isSuccess());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAddUsersToGroupNonVisible()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("SomeOtherGroup"); // only available group to join
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), EasyList.build("fred")).isSuccess());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You cannot add users to groups which are not visible to you.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAddUsersToGroupAleadyAMember()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("SomeOtherGroup", "SomeGroup");
            }

            @Override
            boolean validateUserIsNotInSelectedGroups(final JiraServiceContext jiraServiceContext, final Collection selectedGroupsNames, final User user)
            {
                return false;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            List getUserGroups(final User user)
            {
                return EasyList.build("SomeGroup");
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), EasyList.build("foo")).isSuccess());
    }

    @Test
    public void testValidateAddUsersToGroupHappy()
    {
        final AtomicBoolean validateGroupNamesExistCalled = new AtomicBoolean(false);
        final AtomicBoolean isUserNullCalled = new AtomicBoolean(false);
        final AtomicBoolean isExternalUserManagementEnabledCalled = new AtomicBoolean(false);
        final AtomicBoolean getNonMemberGroupsCalled = new AtomicBoolean(false);
        final AtomicBoolean validateUserIsNotInSelectedGroupsCalled = new AtomicBoolean(false);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {

            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateGroupNamesExistCalled.set(true);
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                isUserNullCalled.set(true);
                return false;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                isExternalUserManagementEnabledCalled.set(true);
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                getNonMemberGroupsCalled.set(true);
                return EasyList.build("SomeOtherGroup", "SomeGroup");
            }

            @Override
            boolean validateUserIsNotInSelectedGroups(final JiraServiceContext jiraServiceContext, final Collection selectedGroupsNames, final User user)
            {
                validateUserIsNotInSelectedGroupsCalled.set(true);
                return true;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }

            @Override
            boolean groupsHaveGlobalUsePermissions(final Collection /* <String> */groupNames)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertTrue(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), EasyList.build("dude")).isSuccess());
        assertTrue(validateGroupNamesExistCalled.get());
        assertTrue(isUserNullCalled.get());
        assertTrue(isExternalUserManagementEnabledCalled.get());
        assertTrue(getNonMemberGroupsCalled.get());
        assertTrue(validateUserIsNotInSelectedGroupsCalled.get());

    }

    @Test
    public void testValidateAddUsersToGroupWillExceedLicenseLimit()
    {
        final AtomicBoolean validateGroupNamesExistCalled = new AtomicBoolean(false);
        final AtomicBoolean isUserNullCalled = new AtomicBoolean(false);
        final AtomicBoolean isExternalUserManagementEnabledCalled = new AtomicBoolean(false);
        final AtomicBoolean getNonMemberGroupsCalled = new AtomicBoolean(false);
        final AtomicBoolean validateUserIsNotInSelectedGroupsCalled = new AtomicBoolean(false);
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.expectAndReturn("canActivateNumberOfUsers", new Constraint[] { P.eq(1) }, Boolean.FALSE);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null,
            (UserUtil) mockUserUtil.proxy(), null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateGroupNamesExistCalled.set(true);
                return true;
            }

            @Override
            boolean isUserNull(final User user)
            {
                isUserNullCalled.set(true);
                return false;
            }

            @Override
            boolean isExternalUserManagementEnabled()
            {
                isExternalUserManagementEnabledCalled.set(true);
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                getNonMemberGroupsCalled.set(true);
                return EasyList.build("SomeOtherGroup", "SomeGroup");
            }

            @Override
            boolean validateUserIsNotInSelectedGroups(final JiraServiceContext jiraServiceContext, final Collection selectedGroupsNames, final User user)
            {
                validateUserIsNotInSelectedGroupsCalled.set(true);
                return true;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }

            @Override
            boolean groupsHaveGlobalUsePermissions(final Collection /* <String> */groupNames)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("SomeGroup"), EasyList.build("dude")).isSuccess());
        assertTrue(validateGroupNamesExistCalled.get());
        assertTrue(isUserNullCalled.get());
        assertTrue(isExternalUserManagementEnabledCalled.get());
        assertTrue(getNonMemberGroupsCalled.get());
        assertTrue(validateUserIsNotInSelectedGroupsCalled.get());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals(
            "Adding the user to the groups you have selected will grant the 'JIRA Users' permission to the user" + " in JIRA. This will exceed the number of users allowed to use JIRA under your license. Please" + " reduce the number of users with the 'JIRA Users', 'JIRA Administrators' or 'JIRA System" + " Administrators' global permissions or consider upgrading your license.",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetGroupNamesUserCanSee()
    {
        final MockControl mockGlobalPermissionGroupAssociationUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermissionGroupAssociationUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermissionGroupAssociationUtilControl.getMock();
        mockGlobalPermissionGroupAssociationUtil.getGroupNamesModifiableByCurrentUser(null, null);
        mockGlobalPermissionGroupAssociationUtilControl.setReturnValue(EasyList.build("FooGroup", "BarGroup"));
        mockGlobalPermissionGroupAssociationUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, mockGlobalPermissionGroupAssociationUtil, null, null,
            null, null, null, null, null, null, null, null, null, null)
        {
            List getAllGroupNames()
            {
                return null;
            }

        };
        final List list = defaultGroupService.getGroupNamesUserCanSee(null);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.contains("BarGroup"));
        assertTrue(list.contains("FooGroup"));
        mockGlobalPermissionGroupAssociationUtilControl.verify();
    }

    @Test
    public void testValidateUserIsNotInSelectedGroupsAlreadyInOnlyOne()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            List getUserGroups(final User user)
            {
                return EasyList.build("AnyGroup");
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final User user = new MockUser("admin");
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateUserIsNotInSelectedGroups(jiraServiceContext, EasyList.build("AnyGroup"), user));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        final String msg = "Cannot add user 'admin', user is already a member of 'AnyGroup'";
        assertEquals(msg, errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUserIsNotInSelectedGroupsAlreadyInAll()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            List getUserGroups(final User user)
            {
                return EasyList.build("AnyGroup", "TestGroup");
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final User user = new MockUser("admin");
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateUserIsNotInSelectedGroups(jiraServiceContext, EasyList.build("AnyGroup", "TestGroup"), user));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        final String msg = "Cannot add user 'admin', user is already a member of all the selected group(s)";
        assertEquals(msg, errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUserIsNotInSelectedGroupsHappy()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            List getUserGroups(final User user)
            {
                return EasyList.build("TestGroup");
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertTrue(defaultGroupService.validateUserIsNotInSelectedGroups(jiraServiceContext, EasyList.build("AnyGroup"), null));
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testIsUserInGroupsInGroups()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            List getUserGroups(final User user)
            {
                return EasyList.build("TestGroup");
            }
        };
        assertTrue(defaultGroupService.isUserInGroups(null, Collections.singleton("TestGroup")));
    }

    @Test
    public void testIsUserInGroupsNotInGroups()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            List getUserGroups(final User user)
            {
                return Collections.singletonList("TestGroup");
            }
        };
        assertFalse(defaultGroupService.isUserInGroups(null, Collections.singleton("OtherGroup")));
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsGroupNamesDontExist()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, null, null, null, true));
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsIsAllGroupNotSelected()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        final User user = new MockUser("admin");
        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user, EasyList.build("Group1"), EasyList.build("Group2"),
            false));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals(
            "Cannot remove user 'admin' from group 'Group2' since the group was not selected. Please make sure to refresh after selecting new group(s)",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsCurrentUserRemovingAdmin()
    {
        final ApplicationUser user = new MockApplicationUser("admin");

        final MockControl mockGlobalPermissionGroupAssociationUtilControl = MockClassControl.createStrictControl(GlobalPermissionGroupAssociationUtil.class);
        final GlobalPermissionGroupAssociationUtil mockGlobalPermissionGroupAssociationUtil = (GlobalPermissionGroupAssociationUtil) mockGlobalPermissionGroupAssociationUtilControl.getMock();
        mockGlobalPermissionGroupAssociationUtil.getSysAdminMemberGroups(user);
        mockGlobalPermissionGroupAssociationUtilControl.setReturnValue(null);
        mockGlobalPermissionGroupAssociationUtil.getAdminMemberGroups(user);
        mockGlobalPermissionGroupAssociationUtilControl.setReturnValue(null);
        mockGlobalPermissionGroupAssociationUtilControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, mockGlobalPermissionGroupAssociationUtil, null, null,
            null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errors)
        {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user.getDirectoryUser(), EasyList.build("Group1"), EasyList.build("Group1"),
            true));
        mockGlobalPermissionGroupAssociationUtilControl.verify();
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsUserCantSeeGroup()
    {

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return Collections.EMPTY_LIST;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, null, EasyList.build("Group1"), EasyList.build("Group1"),
            true));

        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You can not remove a group from this user as it is not visible to you.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsUserNotInGroupNotAll()
    {
        final User user = new MockUser("admin");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("Group1");
            }

            @Override
            boolean isUserInGroups(final User user, final Set /*<String>*/groupNames)
            {
                return false;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user, EasyList.build("Group1"), EasyList.build("Group1"),
            false));

        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot remove user 'admin' from group 'Group1' since user is not a member of 'Group1'",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsUserNotInGroupAllOneGroup()
    {
        final User user = new MockUser("admin");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("Group1");
            }

            @Override
            boolean isUserInGroups(final User user, final Set /*<String>*/groupNames)
            {
                return false;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user, EasyList.build("Group1"), EasyList.build("Group1"),
            true));

        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot remove user 'admin' from group 'Group1' since user is not a member of 'Group1'",
            errors.getErrorMessages().iterator().next());

    }

    @Test
    public void testValidateCanRemoveUserFromGroupsUserNotInGroupAllNotInAll()
    {
        final User user = new MockUser("admin");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("Group1", "Group2");
            }

            @Override
            boolean isUserInGroups(final User user, final Set /*<String>*/groupNames)
            {
                return false;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user, EasyList.build("Group1"), EasyList.build("Group1",
            "Group2"), true));

        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot remove user 'admin' from selected group(s) since user is not a member of all the selected group(s)",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateCanRemoveUserFromGroupsHappyPath()
    {
        final User user = new MockUser("admin");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                return true;
            }

            @Override
            boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
            {
                return false;
            }

            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("Group1", "Group2");
            }

            @Override
            boolean isUserInGroups(final User user, final Set /*<String>*/groupNames)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertTrue(defaultGroupService.validateCanRemoveUserFromGroups(jiraServiceContext, user, EasyList.build("Group1"), EasyList.build("Group1",
            "Group2"), true));
    }

    @Test
    public void testValidateRemoveUsersFromGroupsNullMapper()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        try
        {
            defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, null);
            fail("We should see an exception when a null mapper is passed in.");
        }
        catch (final Exception e)
        {
            // this should happen
            assertEquals("You must specify a non null mapper.", e.getMessage());
        }
    }

    @Test
    public void testValidateRemoveUsersFromGroupsExternalUserManagementEnabled()
    {
        final Mock mockJiraContactHelper = new Mock(JiraContactHelper.class);
        mockJiraContactHelper.expectAndReturn("getAdministratorContactMessage", P.ANY_ARGS, "please contact your JIRA administrators");

        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, (JiraContactHelper) mockJiraContactHelper.proxy(), null)
        {
            @Override
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertFalse(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, new GroupRemoveChildMapper()));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.",
            errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateRemoveUsersFromGroupsUserIsNull()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return true;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        final GroupRemoveChildMapper mapper = new GroupRemoveChildMapper().register(null, "Group1");
        assertFalse(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, mapper));
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("Cannot remove user. 'null' does not exist", errors.getErrorMessages().iterator().next());

    }

    @Test
    public void testValidateRemoveUsersFromGroupsRemoveFromAll()
    {
        final AtomicInteger validateCanRemoveUserCount = new AtomicInteger(0);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean validateCanRemoveUserFromGroups(final JiraServiceContext jiraServiceContext, final User userToRemove, final List allSelectedGroups, final List groupsToLeave, final boolean isAll)
            {
                validateCanRemoveUserCount.incrementAndGet();
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        final GroupRemoveChildMapper mapper = new GroupRemoveChildMapper(EasyList.build("Group1"));
        mapper.register(null);
        assertFalse(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, mapper));
        assertEquals(1, validateCanRemoveUserCount.get());
    }

    @Test
    public void testValidateRemoveUsersFromGroupsRemoveFromGroups()
    {
        final AtomicInteger validateCanRemoveUserCount = new AtomicInteger(0);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean validateCanRemoveUserFromGroups(final JiraServiceContext jiraServiceContext, final User userToRemove, final List allSelectedGroups, final List groupsToLeave, final boolean isAll)
            {
                validateCanRemoveUserCount.incrementAndGet();
                return false;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        final GroupRemoveChildMapper mapper = new GroupRemoveChildMapper(EasyList.build("Group1"));
        mapper.register(null, EasyList.build("Group1", "Group2"));
        assertFalse(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, mapper));
        assertEquals(2, validateCanRemoveUserCount.get());

    }

    @Test
    public void testValidateRemoveUsersFromGroupsHappyPath()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean isUserNull(final User user)
            {
                return false;
            }

            @Override
            boolean validateCanRemoveUserFromGroups(final JiraServiceContext jiraServiceContext, final User userToRemove, final List allSelectedGroups, final List groupsToLeave, final boolean isAll)
            {
                return true;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        final GroupRemoveChildMapper mapper = new GroupRemoveChildMapper(EasyList.build("Group1"));
        mapper.register(null, EasyList.build("Group1", "Group2"));
        assertTrue(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext, mapper));
    }

    @Test
    public void testValidateRemoveUserFromGroupsNullGroupsToLeave()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        try
        {
            defaultGroupService.validateRemoveUserFromGroups(jiraServiceContext, null, null);
            fail();
        }
        catch (final Exception e)
        {
            assertEquals("You must specify a non null groupsToLeave.", e.getMessage());
        }

    }

    @Test
    public void testValidateRemoveUserFromGroupsHappyPath()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            public boolean validateRemoveUsersFromGroups(final JiraServiceContext jiraServiceContext, final GroupRemoveChildMapper mapper)
            {
                assertNotNull(mapper);
                final List groupsForUser = IteratorUtils.toList(mapper.getGroupsIterator("admin"));
                assertNotNull(groupsForUser);
                assertEquals(1, groupsForUser.size());
                assertEquals("Group1", groupsForUser.iterator().next());
                assertFalse(mapper.isRemoveFromAllSelected("admin"));
                return true;
            }
        };

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);

        assertTrue(defaultGroupService.validateRemoveUserFromGroups(jiraServiceContext, EasyList.build("Group1"), "admin"));

    }

    @Test
    public void testAddUsersToGroupsHappy()
    {
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        mockUserUtil.expectVoid("addUserToGroup", P.ANY_ARGS);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null,
            (UserUtil) mockUserUtil.proxy(), null, null, null, null, null)
        {
            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("group1");
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            Collection convertGroupNamesToGroups(final Collection<String> groupNames)
            {
                return Collections.singletonList(new MockGroup("group1"));
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertTrue(defaultGroupService.addUsersToGroups(jiraServiceContext, EasyList.build("group1"), EasyList.build("user1")));

        mockUserUtil.verify();
    }

    @Test
    public void testAddUsersToGroupsNullArgs()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
        try
        {
            defaultGroupService.addUsersToGroups(null, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            fail("expected exception");
        }
        catch (final RuntimeException e)
        {
            //expected
        }

        try
        {
            final SimpleErrorCollection errors = new SimpleErrorCollection();
            final JiraServiceContext jiraServiceContext = getContext(errors);
            defaultGroupService.addUsersToGroups(jiraServiceContext, null, Collections.EMPTY_LIST);
            fail("expected exception");
        }
        catch (final RuntimeException e)
        {
            //expected
        }

        try
        {
            final SimpleErrorCollection errors = new SimpleErrorCollection();
            final JiraServiceContext jiraServiceContext = getContext(errors);
            defaultGroupService.addUsersToGroups(jiraServiceContext, Collections.EMPTY_LIST, null);
            fail("expected exception");
        }
        catch (final RuntimeException e)
        {
            //expected
        }

    }

    @Test
    public void testAddUsersToGroupsNoPermission()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("group2");
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.addUsersToGroups(jiraServiceContext, EasyList.build("group1"), EasyList.build("user1")));
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You cannot add users to groups which are not visible to you.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testAddUsersToGroupNotAdmin()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.addUsersToGroups(jiraServiceContext, EasyList.build("group1"), EasyList.build("user1")));
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testRemoveUsersFromGroupNotAdmin()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.removeUsersFromGroups(jiraServiceContext,
            new GroupRemoveChildMapper(EasyList.build("group1")).register("user1")));
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateAddUsersToGroupNotAdmin()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateAddUsersToGroup(jiraServiceContext, EasyList.build("group1"), EasyList.build("user1")).isSuccess());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateRemoveUsersFromGroupNotAdmin()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return false;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        assertFalse(defaultGroupService.validateRemoveUsersFromGroups(jiraServiceContext,
            new GroupRemoveChildMapper(EasyList.build("group1")).register("user1")));
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You must be at least a JIRA Administrator to manipulate a group.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testRemoveUsersFromGroupsHappy()
    {
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        mockUserUtil.expectVoid("removeUserFromGroup", P.ANY_ARGS);
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null,
            (UserUtil) mockUserUtil.proxy(), null, null, null, null, null)
        {
            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("group1");
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            Collection convertGroupNamesToGroups(final Collection groupNames)
            {
                return Collections.singletonList(new MockGroup("group1"));
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        final GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper();
        groupRemoveChildMapper.register("User1", "group1");
        assertTrue(defaultGroupService.removeUsersFromGroups(jiraServiceContext, groupRemoveChildMapper));

        mockUserUtil.verify();
    }

    @Test
    public void testRemoveUsersFromGroupsNullArgs()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
        try
        {
            defaultGroupService.removeUsersFromGroups(null, new GroupRemoveChildMapper());
            fail("expected exception");
        }
        catch (final RuntimeException e)
        {
            //expected
        }

        try
        {
            final SimpleErrorCollection errors = new SimpleErrorCollection();
            final JiraServiceContext jiraServiceContext = getContext(errors);
            defaultGroupService.removeUsersFromGroups(jiraServiceContext, null);
            fail("expected exception");
        }
        catch (final RuntimeException e)
        {
            //expected
        }

    }

    @Test
    public void testRemoveUsersFromGroupsNoPermission()
    {
        final DefaultGroupService defaultGroupService = new DefaultGroupService(null, null, null, null, null, null, null, null, null,
            null, null, null, null, null)
        {
            @Override
            List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
            {
                return EasyList.build("group2");
            }

            @Override
            User getUser(final String userName)
            {
                return null;
            }

            @Override
            boolean userHasAdminPermission(final User user)
            {
                return true;
            }
        };
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = getContext(errors);
        final GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper();
        groupRemoveChildMapper.register("user1", "group1");
        assertFalse(defaultGroupService.removeUsersFromGroups(jiraServiceContext, groupRemoveChildMapper));
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("You can not remove a group from this user as it is not visible to you.", errors.getErrorMessages().iterator().next());
    }

    @Test
    public void testGroupsHaveGlobalUsePermissions()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();

        // order of calls is important, so we need to loop through the 3 different parameters
        for (int i = 0; i < 3; i++)
        {
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER),
                Collections.EMPTY_LIST, 1);
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.USE), Collections.EMPTY_LIST, 1);
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN),
                EasyList.build("testGroup3"), 1);
        }

        mockGlobalPermissionManagerControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, null, null, null, null, null,
            null, null, null, null, null, null, null, null);
        assertFalse(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup1", "testGroup2")));
        assertTrue(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup3")));
        assertTrue(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup2", "testGroup3")));

        mockGlobalPermissionManagerControl.verify();

    }

    @Test
    public void testGroupsHaveGlobalUsePermissionsWithAllGroups()
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();

        for (int i = 0; i < 5; i++)
        {
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.ADMINISTER),
                EasyList.build("testGroup2"), 1);
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.USE),
                EasyList.build("testGroup1"), 1);
            mockGlobalPermissionManagerControl.expectAndReturn(mockGlobalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN),
                EasyList.build("testGroup3"), 1);
        }
        mockGlobalPermissionManagerControl.replay();

        final DefaultGroupService defaultGroupService = new DefaultGroupService(mockGlobalPermissionManager, null, null, null, null, null,
            null, null, null, null, null, null, null, null);
        assertTrue(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup1")));
        assertTrue(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup2")));
        assertTrue(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup3")));
        assertFalse(defaultGroupService.groupsHaveGlobalUsePermissions(EasyList.build("testGroup4")));
        assertFalse(defaultGroupService.groupsHaveGlobalUsePermissions(Collections.EMPTY_LIST));

        mockGlobalPermissionManagerControl.verify();
    }
}
