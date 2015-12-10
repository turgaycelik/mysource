package com.atlassian.jira.issue.comments;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDefaultCommentPermissionManager
{
    @Test
    public void testIsUserCommentAuthor() throws Exception
    {
        Comment anonComment = new MockComment(null, "Blah");
        Comment fredComment = new MockComment("fred", "Blah");
        ApplicationUser fred = new MockApplicationUser("fred");
        ApplicationUser dude = new MockApplicationUser("dude");
        // Class under test
        DefaultCommentPermissionManager defaultCommentPermissionManager = new DefaultCommentPermissionManager(null, null, null);
        // ApplicationUser is anonymous therefore is NEVER the author
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor((ApplicationUser) null, fredComment));
        // Comment is anonymous therefore no-one is the author
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(fred, anonComment));
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor((ApplicationUser) null, anonComment));
        // Different ApplicationUser
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(dude, fredComment));
        // Same ApplicationUser
        assertTrue(defaultCommentPermissionManager.isUserCommentAuthor(fred, fredComment));
    }

    @Test
    public void testHasEditAllPermission()
    {
        ApplicationUser user = new MockApplicationUser("Bob");
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
                P.args(new IsEqual(new Integer(Permissions.COMMENT_EDIT_ALL)), new IsAnything(), new IsEqual(user)), Boolean.TRUE);
        PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, permissionManager, null);
        assertTrue(manager.hasEditAllPermission(user, null));

        mockPermissionManager.verify();
    }

    @Test
    public void testHasEditAllPermissionFalse()
    {
        ApplicationUser user = new MockApplicationUser("Bob");
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
                P.args(new IsEqual(new Integer(Permissions.COMMENT_EDIT_ALL)), new IsAnything(), new IsEqual(user)), Boolean.FALSE);
        PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, permissionManager, null);
        assertFalse(manager.hasEditAllPermission(user, null));

        mockPermissionManager.verify();
    }

    @Test
    public void testHasBrowsePermissionGloballyVisible()
    {
        ApplicationUser user = new MockApplicationUser("Bob");
        Comment comment = new MockComment("dude", "comment body");

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, null, null);

        assertTrue(manager.hasBrowsePermission(user, comment));
    }

    @Test
    public void testHasBrowsePermissionGroup()
    {
        ApplicationUser user = new MockApplicationUser("Bob");
        Comment comment = new MockComment("dude", "comment body", "dudes", null);
        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addGroup("dudes");

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, null, mockGroupManager);

        // user not in the group, so bad luck
        assertFalse(manager.hasBrowsePermission(user, comment));
        // Now add him to group
        mockGroupManager.addMember("dudes", "Bob");
        assertTrue(manager.hasBrowsePermission(user, comment));
    }

    //TODO: Write test for Project Role Permission.

}
