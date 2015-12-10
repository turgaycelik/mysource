package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareType.Name;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.jira.sharing.TestSharePermissionImpl}.
 * 
 * @since v4.o
 */

public class TestSharePermissionImpl
{
    private static final String PARAM2 = "param2";
    private static final ShareType.Name TYPE = new Name("type");
    private static final String PARAM1 = "param1";
    private static final Long ID = new Long(1);

    @Test
    public void testSharePermissionImplWithNullType()
    {
        try
        {
            new SharePermissionImpl(null, TestSharePermissionImpl.PARAM1, TestSharePermissionImpl.PARAM2);
            fail("Should not accept null type.");
        }
        catch (final IllegalArgumentException e)
        {
            // this is expected.
        }
    }

    @Test
    public void testSharePermissionImplWithIllegalParams()
    {
        try
        {
            new SharePermissionImpl(TestSharePermissionImpl.TYPE, null, TestSharePermissionImpl.PARAM2);
            fail("Should not accept non null param2 when param1 is also null.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testSharePermissionImplWithGoodParams()
    {
        SharePermission perm = new SharePermissionImpl(TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, null);
        assertNull(perm.getId());
        assertEquals(TestSharePermissionImpl.TYPE, perm.getType());
        assertEquals(TestSharePermissionImpl.PARAM1, perm.getParam1());
        assertNull(perm.getParam2());

        perm = new SharePermissionImpl(TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, TestSharePermissionImpl.PARAM2);
        assertNull(perm.getId());
        assertEquals(TestSharePermissionImpl.TYPE, perm.getType());
        assertEquals(TestSharePermissionImpl.PARAM1, perm.getParam1());
        assertEquals(TestSharePermissionImpl.PARAM2, perm.getParam2());

        perm = new SharePermissionImpl(TestSharePermissionImpl.ID, TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, TestSharePermissionImpl.PARAM2);
        assertEquals(TestSharePermissionImpl.ID, perm.getId());
        assertEquals(TestSharePermissionImpl.TYPE, perm.getType());
        assertEquals(TestSharePermissionImpl.PARAM1, perm.getParam1());
        assertEquals(TestSharePermissionImpl.PARAM2, perm.getParam2());
    }

    @Test
    public void testSharePermissionEquals()
    {
        final SharePermission perm1 = new SharePermissionImpl(TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, null);
        final SharePermission perm2 = new SharePermissionImpl(TestSharePermissionImpl.ID, TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, null);
        final SharePermission perm3 = new SharePermissionImpl(TestSharePermissionImpl.TYPE, TestSharePermissionImpl.PARAM1, TestSharePermissionImpl.PARAM2);

        assertEquals(perm1, perm1);
        assertEquals(perm2, perm2);
        assertEquals(perm3, perm3);

        assertEquals(perm1, perm2);
        assertFalse(perm1.equals(perm3));
        assertFalse(perm2.equals(perm3));
        assertFalse(perm2.equals(null));
    }
}
