package com.atlassian.jira.avatar;

import java.util.NoSuchElementException;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for static and base Avatar management stuff in {@link Avatar.Size}
 *
 * @since v6.0
 */
public class TestAvatarSize
{
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void testRequestingAvatarSizeAtKnownPixelsReturnsThatSize()
    {
        for (Avatar.Size size : Avatar.Size.values())
        {
            final Avatar.Size bigger = Avatar.Size.biggerThan(size.getPixels());
            final Avatar.Size smaller = Avatar.Size.biggerThan(size.getPixels());
            assertThat(smaller, allOf(equalTo(size), equalTo(bigger)));
            assertThat(bigger, allOf(equalTo(size), equalTo(smaller)));
        }
    }

    @Test
    public void testRequestingSmallerThanLargestSizeReturnsASize()
    {
        assertThat(Avatar.Size.smallerThan(Avatar.Size.largest().getPixels() + 1), equalTo(Avatar.Size.largest()));
    }

    @Test
    /**
     * @note This isn't ideal behaviour, but it's what the {@link com.atlassian.sal.api.user.UserProfile#getProfilePictureUri(int, int)} docs specify.
     */
    public void testRequestingSmallerThanSmallestSizeReturnsNull()
    {
        assertThat(Avatar.Size.smallerThan(1), equalTo(null));
    }

    @Test
    public void testRequestingLargerThanSmallestSizeReturnsASize()
    {
        assertThat(Avatar.Size.biggerThan(Avatar.Size.MEDIUM.getPixels() + 1), equalTo(Avatar.Size.LARGE));
    }

    @Test
    /**
     * @note This isn't ideal behaviour, but it's what the {@link com.atlassian.sal.api.user.UserProfile#getProfilePictureUri(int, int)} docs specify.
     */
    public void testRequestingLargerThanLargestSizeReturnsNull()
    {
        assertThat(Avatar.Size.biggerThan(Avatar.Size.largest().getPixels() + 1), equalTo(null));
    }

    @Test
    public void shouldReturnPassedSizeFromParam()
    {
        final Avatar.Size spec = Avatar.Size.RETINA_XXLARGE;
        final Avatar.Size size = Avatar.Size.getSizeFromParam(spec.getParam());

        assertThat(size, is(equalTo(spec)));
    }

    @Test
    public void shouldReturnNullForNonExistingSize()
    {
        thrown.expect(NoSuchElementException.class);
        final Avatar.Size size = Avatar.Size.getSizeFromParam("i-bet-this-not-exists");
    }

    @Test
    public void shouldReturnNullForWhenReceivesNull()
    {
        thrown.expect(NoSuchElementException.class);
        final Avatar.Size size = Avatar.Size.getSizeFromParam(null);
    }
}
