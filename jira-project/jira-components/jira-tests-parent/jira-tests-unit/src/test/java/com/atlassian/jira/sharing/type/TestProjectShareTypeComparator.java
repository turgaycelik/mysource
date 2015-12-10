package com.atlassian.jira.sharing.type;

import java.util.Comparator;

import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.sharing.type.ProjectSharePermissionComparator}.
 *
 * @since v3.13
 */

public class TestProjectShareTypeComparator
{
    private static final Project PROJECT_1 = new MockProject(1000, "aaa", "aaa");
    private static final Project PROJECT_2 = new MockProject(1001, "bbb", "bbb");
    private static final Project PROJECT_3 = new MockProject(1002, "ccc", "ccc");

    private static final ProjectRole ROLE_1 = new MockProjectRoleManager.MockProjectRole(100, "zzz", "bbb");
    private static final ProjectRole ROLE_2 = new MockProjectRoleManager.MockProjectRole(101, "yyy", "aaa");

    private static final SharePermission PROJECT_PERM_1 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_1.getId().toString(), null);
    private static final SharePermission PROJECT_PERM_2 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_1.getId().toString(), null);
    private static final SharePermission PROJECT_PERM_3 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_2.getId().toString(), null);
    private static final SharePermission PROJECT_PERM_4 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_3.getId().toString(), null);
    private static final SharePermission PROJECT_PERM_NULL = new SharePermissionImpl(ProjectShareType.TYPE, null, null);
    private static final SharePermission ROLE_PERM_1 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_2.getId().toString(), ROLE_1.getId().toString());
    private static final SharePermission ROLE_PERM_2 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_2.getId().toString(), ROLE_1.getId().toString());
    private static final SharePermission ROLE_PERM_3 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_2.getId().toString(), ROLE_2.getId().toString());
    private static final SharePermission ROLE_PERM_4 = new SharePermissionImpl(ProjectShareType.TYPE, PROJECT_1.getId().toString(), ROLE_2.getId().toString());
    private static final SharePermission GLOBAL_PERMISSION = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

    private ProjectManager projectManager;
    private ProjectRoleManager projectRoleManager;
    private Comparator comparator;

    @Before
    public void setUp() throws Exception
    {
        projectManager = createProjectManager();
        projectRoleManager = createProjectRoleManager();
        comparator = new ProjectSharePermissionComparator(projectManager, projectRoleManager);
    }

    @After
    public void tearDown() throws Exception
    {
        projectManager = null;
        projectRoleManager = null;
    }

    @Test
    public void testConstructionWithNullProjectManager()
    {
        try
        {
            new ProjectSharePermissionComparator(null, projectRoleManager);
            fail("Constructor should not accept null argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }
    @Test
    public void testConstructionWithNullProjectRoleManager()
    {
        try
        {
            new ProjectSharePermissionComparator(projectManager, null);
            fail("Constructor should not accept null argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCompareSameProject()
    {
        assertEquals(0, comparator.compare(PROJECT_PERM_1, PROJECT_PERM_1));
    }

    @Test
    public void testCompareEqualsProject()
    {
        assertEquals(0, comparator.compare(PROJECT_PERM_1, PROJECT_PERM_2));
    }

    @Test
    public void testCompareLessThanProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_1, PROJECT_PERM_3) < 0);
    }

    @Test
    public void testCompareGreaterThanProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_3, PROJECT_PERM_2) > 0);
    }

    @Test
    public void testCompareLessThanProjectNull()
    {
        assertTrue(comparator.compare(PROJECT_PERM_NULL, PROJECT_PERM_2) < 0);
    }

    @Test
    public void testCompareGrearerThanProjectNull()
    {
        assertTrue(comparator.compare(PROJECT_PERM_4, PROJECT_PERM_NULL) > 0);
    }

    @Test
    public void testCompareLessThanProjectAndRoleWithNullProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_NULL, ROLE_PERM_1) < 0);
    }

    @Test
    public void testCompareLessThanProjectAndRoleWithSameProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_3, ROLE_PERM_1) < 0);
    }

    @Test
    public void testCompareLessThanProjectAndRoleWithDifferentProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_1, ROLE_PERM_1) < 0);
    }

    @Test
    public void testCompareGreaterThanProjectAndRoleWithDifferentProject()
    {
        assertTrue(comparator.compare(PROJECT_PERM_4, ROLE_PERM_1) > 0);
    }

    @Test
    public void testCompateGreaterThanRoleAndProjectWithSameProject()
    {
        assertTrue(comparator.compare(ROLE_PERM_1, PROJECT_PERM_3) > 0);
    }

    @Test
    public void testCompareLessThanRoleAndProjectWithDifferentProject()
    {
        assertTrue(comparator.compare(ROLE_PERM_1, PROJECT_PERM_4) < 0);
    }

    @Test
    public void testCompareGreaterThanRoleAndProjectWithDifferentProject()
    {
        assertTrue(comparator.compare(ROLE_PERM_4, PROJECT_PERM_2) > 0);
    }

    @Test
    public void testCompareEqualsRoles()
    {
        assertEquals(0, comparator.compare(ROLE_PERM_1, ROLE_PERM_2));
    }

    @Test
    public void testCompareSameRole()
    {
        assertEquals(0, comparator.compare(ROLE_PERM_1, ROLE_PERM_1));
    }

    @Test
    public void testCompareLessThanRole()
    {
        assertTrue(comparator.compare(ROLE_PERM_3, ROLE_PERM_1) < 0);
    }

    @Test
    public void testCompareGreaterThanRole()
    {
        assertTrue(comparator.compare(ROLE_PERM_1, ROLE_PERM_3) > 0);
    }

    @Test
    public void testCompareWithNullFirst()
    {
        assertTrue(comparator.compare(null, PROJECT_PERM_1) < 0);
    }

    @Test
    public void testCompareWithNullSecond()
    {
        assertTrue(comparator.compare(PROJECT_PERM_1, null) > 0);
    }

    @Test
    public void testCompareWithNulls()
    {
        assertEquals(0, comparator.compare(null, null));
    }

    @Test
    public void testCompareInvalidPermission()
    {
        try
        {
            comparator.compare(GLOBAL_PERMISSION, PROJECT_PERM_1);
            fail("Should not accept invalid permissions type.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCompareInvalidType()
    {
        try
        {
            comparator.compare("aa", PROJECT_PERM_1);
            fail("Should not accept invalid permissions type.");
        }
        catch (ClassCastException e)
        {
            //expected.
        }
    }

    private ProjectManager createProjectManager()
    {
        MockProjectManager mockMgr = new MockProjectManager();
        mockMgr.addProject(PROJECT_1);
        mockMgr.addProject(PROJECT_2);
        mockMgr.addProject(PROJECT_3);

        return mockMgr;
    }

    private ProjectRoleManager createProjectRoleManager()
    {
        MockProjectRoleManager mockMgr = new MockProjectRoleManager();
        mockMgr.addRole(ROLE_1);
        mockMgr.addRole(ROLE_2);

        return mockMgr;
    }
}
