package com.atlassian.jira.sharing.type;

import java.util.Comparator;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareType.Name;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link com.atlassian.jira.sharing.type.DefaultSharePermissionComparator}.
 * 
 * @since v3.13
 */
public class TestDefaultShareTypeComparator
{
    private final SharePermission PERM1 = new SharePermissionImpl(new Name("group"), null, null);
    private final SharePermission PERM2 = new SharePermissionImpl(new Name("group"), "aaa", null);
    private final SharePermission PERM3 = new SharePermissionImpl(new Name("abc"), null, null);
    private final Comparator comparator = new DefaultSharePermissionComparator(new Name("group"));
    private final DefaultSharePermissionComparator shareComparator = (DefaultSharePermissionComparator) comparator;

    @Test
    public void testConstructionWithNullType()
    {
        try
        {
            new DefaultSharePermissionComparator(null);
            fail("Null type should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testCompareSamePermissions()
    {
        assertEquals(0, comparator.compare(PERM1, PERM1));
    }

    @Test
    public void testCompareSameTypePermissions()
    {
        assertEquals(0, comparator.compare(PERM1, PERM1));
    }

    @Test
    public void testDifferentTypePermissions()
    {
        try
        {
            comparator.compare(PERM3, PERM1);
            fail("Should not be able to compare Permissions of different types.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testNullPermissions()
    {
        assertEquals(0, comparator.compare(null, null));
    }

    @Test
    public void testNullFirstPermission()
    {
        assertTrue(comparator.compare(null, PERM2) < 0);
    }

    @Test
    public void testNullSecondPermission()
    {
        assertTrue(comparator.compare(PERM1, null) > 0);
    }

    @Test
    public void testWrongClassType()
    {
        try
        {
            comparator.compare("aa", "bb");
            fail("Should not be able to compare objects of incorrect type.");
        }
        catch (final ClassCastException e)
        {
            // expected.
        }
    }

    @Test
    public void testComparePermissions()
    {
        assertEquals(0, shareComparator.comparePermissions(PERM1, PERM2));
    }

    @Test
    public void testCompareNull()
    {
        assertEquals(0, DefaultSharePermissionComparator.compareNull("aa", "bb"));
    }

    @Test
    public void testCompareNullBothNull()
    {
        assertEquals(0, DefaultSharePermissionComparator.compareNull(null, null));
    }

    @Test
    public void testCompareNullWithNullFirst()
    {
        assertTrue(DefaultSharePermissionComparator.compareNull(null, "aaa") < 0);
    }

    @Test
    public void testCompareNullWithNullSecond()
    {
        assertTrue(DefaultSharePermissionComparator.compareNull("bbb", null) > 0);
    }
}
