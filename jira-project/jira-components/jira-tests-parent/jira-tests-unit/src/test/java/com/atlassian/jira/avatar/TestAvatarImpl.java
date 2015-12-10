package com.atlassian.jira.avatar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.jira.avatar.AvatarImpl}.
 *
 * @since v4.0
 */
public class TestAvatarImpl
{
    @Test
    public void testNullFilename()
    {
        try
        {
            new AvatarImpl(null, null, null, Avatar.Type.PROJECT, "jamiroquai", false);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testNullContentType()
    {
        try
        {
            new AvatarImpl(null, "file.png", null, null, "jamiroquai", false);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testNullType()
    {
        try
        {
            new AvatarImpl(null, "file.png", "image/png", null, "jamiroquai", false);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testNullOwner()
    {
        try
        {
            new AvatarImpl(null, "file.png", "image/png", Avatar.Type.PROJECT, null, false);
            fail("expected IAE");
        }
        catch (IllegalArgumentException yay)
        {

        }
    }

    @Test
    public void testNullOwnerSystem()
    {
        new AvatarImpl(null, "file.png", "image/png", Avatar.Type.PROJECT, null, true);
    }

    @Test
    public void testHappy()
    {
        Avatar a = new AvatarImpl(23L, "filename.foo", "image/png", Avatar.Type.PROJECT, "bobmarley", false);
        assertEquals(new Long(23L), a.getId());
        assertEquals("filename.foo", a.getFileName());
        assertEquals("image/png", a.getContentType());
        assertEquals("project", a.getAvatarType().getName());
        assertEquals("bobmarley", a.getOwner());
        assertEquals(false, a.isSystemAvatar());
    }
}
