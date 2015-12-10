package com.atlassian.jira.avatar;

import org.junit.Test;

import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link com.atlassian.jira.avatar.Avatar}.
 *
 * @since v4.0
 */
public class TestAvatar
{
    @Test
    public void testAvatarType()
    {
        assertNull(Avatar.Type.getByName(null));
    }


    @Test
    public void testAvatarDefaultSize() throws Exception
    {
        for (Avatar.Size size : Avatar.Size.values())
        {
            assertEquals(size.isDefault ? size.toString() + " is marked as default, while Avatar.Size.defaultSize() returns " + Avatar.Size.defaultSize()
                    : size.toString() + " has isDefault flag set to false, while Avatar.Size.defaultSize() indicates it as default.",
                    size.isDefault, size == Avatar.Size.defaultSize());
        }
    }

    @Test
    public void testAvatarLargestSize() throws Exception
    {
        for (Avatar.Size size : Avatar.Size.values())
        {
            assertThat(size.toString() + " is bigger than indicated by Avatar.Size.largest() which is " + Avatar.Size.largest(),
                    size.getPixels(), lessThanOrEqualTo(Avatar.Size.largest().getPixels()));
        }
    }

    @Test
    public void testAvatarSizeInSyncWithImageSize() throws Exception
    {
        assertEquals("Default sizes do not match between Avatar and Image size.",
                Avatar.Size.defaultSize().getPixels(), AvatarManager.ImageSize.defaultSize().getPixels());
        assertEquals("Largest sizes do not match between Avatar and Image size.",
                Avatar.Size.largest().getPixels(), AvatarManager.ImageSize.largest().getPixels());

    }



}
