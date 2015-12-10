package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ProjectShareType;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestSharePermissionDeleteUtils
{
    private static final SharePermissionImpl GROUP_PERMISSIONS = new SharePermissionImpl(GroupShareType.TYPE, "groupname1", null);
    private static final Long ROLE_ID = new Long(1);
    private static final Long PROJECT_ID = new Long(99);

    @Test
    public void testDudInput() throws Exception
    {
        SharePermissionDeleteUtils deleteUtils = new SharePermissionDeleteUtils(null);
        try
        {
            deleteUtils.deleteGroupPermissions(null);
            fail("should have barfed");
        }
        catch (IllegalArgumentException e)
        {
        }
        try
        {
            deleteUtils.deleteRoleSharePermissions(null);
            fail("should have barfed");
        }
        catch (IllegalArgumentException e)
        {
        }
        try
        {
            deleteUtils.deleteProjectSharePermissions(null);
            fail("should have barfed");
        }
        catch (IllegalArgumentException e)
        {
        }


    }

    @Test
    public void testDeleteGroup() throws Exception
    {
        MockControl shareManagerMockCtrl = MockControl.createStrictControl(ShareManager.class);
        ShareManager shareManager = (ShareManager) shareManagerMockCtrl.getMock();
        shareManager.deleteSharePermissionsLike(GROUP_PERMISSIONS);
        shareManagerMockCtrl.replay();

        SharePermissionDeleteUtils deleteUtils = new SharePermissionDeleteUtils(shareManager);
        deleteUtils.deleteGroupPermissions("groupname1");

        shareManagerMockCtrl.verify();
    }

    @Test
    public void testDeleteProjectRole() throws Exception
    {
        SharePermission  roleSharePermission = new SharePermissionImpl(ProjectShareType.TYPE,ROLE_ID.toString());
        
        MockControl shareManagerMockCtrl = MockControl.createStrictControl(ShareManager.class);
        ShareManager shareManager = (ShareManager) shareManagerMockCtrl.getMock();
        shareManager.deleteSharePermissionsLike(roleSharePermission);
        shareManagerMockCtrl.replay();

        SharePermissionDeleteUtils deleteUtils = new SharePermissionDeleteUtils(shareManager);
        deleteUtils.deleteRoleSharePermissions(ROLE_ID);

        shareManagerMockCtrl.verify();

    }

    @Test
    public void testDeleteProject() throws Exception
    {
        SharePermission permission = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_ID.toString(), null);

        MockControl shareManagerMockCtrl = MockControl.createStrictControl(ShareManager.class);
        ShareManager shareManager = (ShareManager) shareManagerMockCtrl.getMock();
        shareManager.deleteSharePermissionsLike(permission);
        shareManagerMockCtrl.replay();

        SharePermissionDeleteUtils deleteUtils = new SharePermissionDeleteUtils(shareManager);
        deleteUtils.deleteProjectSharePermissions(PROJECT_ID);

        shareManagerMockCtrl.verify();

    }
}
