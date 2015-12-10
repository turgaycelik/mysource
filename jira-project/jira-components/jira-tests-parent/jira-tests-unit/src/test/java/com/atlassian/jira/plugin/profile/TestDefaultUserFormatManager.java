package com.atlassian.jira.plugin.profile;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.plugin.userformat.DefaultUserFormatManager;
import com.atlassian.jira.plugin.userformat.UserFormats;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestDefaultUserFormatManager
{
    private UserFormats mockUserFormats;

    @Before
    public void setUpMocks()
    {
        mockUserFormats = EasyMock.createMock(UserFormats.class);
    }

    @Test
    public void formatUserShouldFormatAccordingToTheUserFormatForTheSpecifiedType()
    {
        final String expectedFormatOutputForUser = "<profile>john</profile>";
        final UserFormat userFormatForSpecifiedType = EasyMock.createMock(UserFormat.class);
        expect(userFormatForSpecifiedType.format("john", "someid")).andReturn(expectedFormatOutputForUser).once();

        expect(mockUserFormats.forType("specified-type")).andStubReturn(userFormatForSpecifiedType);

        replay(mockUserFormats, userFormatForSpecifiedType);

        final DefaultUserFormatManager userFormatManager = new DefaultUserFormatManager(mockUserFormats);

        final String actualFormatOutputForUser = userFormatManager.formatUser("john", "specified-type", "someid");

        assertEquals(actualFormatOutputForUser, expectedFormatOutputForUser);
        verify(mockUserFormats, userFormatForSpecifiedType);
    }

    @Test
    public void formatUserShouldReturnNullWhenTheSpecifiedTypeHasNoUserFormat()
    {
        expect(mockUserFormats.forType("invalidType")).andReturn(null);

        DefaultUserFormatManager userFormatManager = new DefaultUserFormatManager(mockUserFormats);
        final String formattedUser = userFormatManager.formatUser("john", "invalidType", "someid");

        assertNull(formattedUser);
    }

    @Test
    public void formatUserWithParametersShouldFormatAccordingToTheUserFormatForTheSpecifiedType() throws Exception
    {
        final Map<String, Object> parametersMap = Collections.emptyMap();
        final String expectedFormatOutputForUser = "<profile>john</profile>";
        final UserFormat userFormatForSpecifiedType = EasyMock.createMock(UserFormat.class);
        expect(userFormatForSpecifiedType.format("john", "someid", parametersMap)).andReturn(expectedFormatOutputForUser).once();

        expect(mockUserFormats.forType("specified-type")).andStubReturn(userFormatForSpecifiedType);

        replay(mockUserFormats, userFormatForSpecifiedType);

        final DefaultUserFormatManager userFormatManager = new DefaultUserFormatManager(mockUserFormats);

        final String actualFormatOutputForUser =
                userFormatManager.formatUser("john", "specified-type", "someid", parametersMap);

        assertEquals(actualFormatOutputForUser, expectedFormatOutputForUser);
        verify(mockUserFormats, userFormatForSpecifiedType);
    }

    @Test
    public void formatUserWithParametersShouldReturnNullWhenTheSpecifiedTypeHasNoUserFormat() throws Exception
    {
        final Map<String, Object> parametersMap = Collections.emptyMap();
        expect(mockUserFormats.forType("invalidType")).andReturn(null);

        DefaultUserFormatManager userFormatManager = new DefaultUserFormatManager(mockUserFormats);
        final String formattedUser = userFormatManager.formatUser("john", "invalidType", "someid", parametersMap);

        assertNull(formattedUser);
    }
}
