package com.atlassian.jira.avatar;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by dszuksztul on 3/10/14.
 */
public class TestAvatarType
{
    @Test
    public void shouldReturnNullTypeForNullName() {
        // when
        final Avatar.Type avatarTypeByName = Avatar.Type.getByName(null);

        // expect
        assertThat(avatarTypeByName, is(nullValue()));
    }

    @Test
    public void shouldReturnNullTypeForNonExistingName() {
        // when
        final Avatar.Type avatarTypeByName = Avatar.Type.getByName("s0m3-n0n-ex1st1ng");

        // expect
        assertThat(avatarTypeByName, is(nullValue()));
    }

    @Test
    public void shouldReturnTypeFromItsName() {
        // when
        final Avatar.Type avatarTypeByName = Avatar.Type.getByName(Avatar.Type.PROJECT.getName());

        // expect
        assertThat(avatarTypeByName, is(Avatar.Type.PROJECT));
    }
}
