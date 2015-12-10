package com.atlassian.jira.sharing.type;

import java.util.Comparator;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.sharing.type.GroupShareTypeRenderer}.
 *
 * @since v3.13
 */
public class TestGroupShareTypeComparator
{
    private final SharePermission GROUP_PERM_NULL = new SharePermissionImpl(GroupShareType.TYPE, null, null);
    private final SharePermission GROUP_PERM2 = new SharePermissionImpl(GroupShareType.TYPE, "aaa", null);
    private final SharePermission GROUP_PERM3 = new SharePermissionImpl(GroupShareType.TYPE, "aaa", null);
    private final SharePermission GROUP_PERM4 = new SharePermissionImpl(GroupShareType.TYPE, "bbb", null);
    private final SharePermission GLOBAL_PERMISSION = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

    private final DefaultSharePermissionComparator shareComparator = new GroupSharePermissionComparator();
    private final Comparator comparator = shareComparator;

    @Test
    public void testCompareSamePermissions()
    {
        assertEquals(0, comparator.compare(GROUP_PERM2, GROUP_PERM2));
    }

    @Test
    public void testCompareEqualPermissions()
    {
        assertEquals(0, comparator.compare(GROUP_PERM2, GROUP_PERM3));
    }

    @Test
    public void testCompareLessThanPermissions()
    {
        assertTrue(comparator.compare(GROUP_PERM2, GROUP_PERM4) < 0);
    }

    @Test
    public void testCompareGreaterThanPermissions()
    {
        assertTrue(comparator.compare(GROUP_PERM4, GROUP_PERM2) > 0);
    }

    @Test
    public void testCompareLessThanPermissionsWithNullGroup()
    {
        assertTrue(comparator.compare(GROUP_PERM_NULL, GROUP_PERM4) < 0);
    }

    @Test
    public void testCompareGreaterThanPermissionsWithNullGroup()
    {
        assertTrue(comparator.compare(GROUP_PERM4, GROUP_PERM_NULL) > 0);
    }

    @Test
    public void testCompareWithNullFirst()
    {
        assertTrue(comparator.compare(null, GROUP_PERM_NULL) < 0);
    }

    @Test
    public void testCompareWithNullSecond()
    {
        assertTrue(comparator.compare(GROUP_PERM_NULL, null) > 0);
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
            comparator.compare(GLOBAL_PERMISSION, GROUP_PERM2);
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
            comparator.compare("aa", GROUP_PERM2);
            fail("Should not accept invalid permissions type.");
        }
        catch (ClassCastException e)
        {
            //expected.
        }
    }
}
