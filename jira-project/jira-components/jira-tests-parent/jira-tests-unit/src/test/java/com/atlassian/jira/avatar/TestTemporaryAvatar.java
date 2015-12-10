package com.atlassian.jira.avatar;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.jira.avatar.TemporaryAvatar}.
 *
 * @since v4.0
 */
public class TestTemporaryAvatar
{
    @Test
    public void testConstructor()
    {

        try
        {
            new TemporaryAvatar(null, null, null, null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

        try
        {
            new TemporaryAvatar("content/type", "content/type", null, null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

        try
        {
            new TemporaryAvatar("content/type", "content/type", "originalFilename", null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

    }

    /** Super basic */
    @Test
    public void testGetters() {
        final File file = new File("/");
        TemporaryAvatar ta = new TemporaryAvatar("content/type", "content/type", "originalFilename", file, null);
        assertEquals("content/type", ta.getContentType());
        assertEquals("originalFilename", ta.getOriginalFilename());
        assertEquals(file, ta.getFile());
    }

}
