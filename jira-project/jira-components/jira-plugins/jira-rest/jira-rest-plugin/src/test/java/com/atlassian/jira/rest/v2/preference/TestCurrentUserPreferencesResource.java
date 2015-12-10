package com.atlassian.jira.rest.v2.preference;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.matchers.ErrorMatchers;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.google.common.base.Function;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;


public class TestCurrentUserPreferencesResource
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String NONEXISTENT_KEY = "NonexistentKey";
    private static final String USER = "user";
    private static final String SOME_VALUE = "someValue";
    private static final String SOME_KEY = "someKey";

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private UserPreferencesManager userPreferencesManager;

    @Mock
    private ExtendedPreferences jiraUserPreferences;

    @Rule
    public InitMockitoMocks intiMocks = new InitMockitoMocks(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    private CurrentUserPreferencesResource preferencesResource;

    @Before
    public void setUp() throws Exception
    {
        preferencesResource = new CurrentUserPreferencesResource(jiraAuthenticationContext, userPreferencesManager);
        final ApplicationUser applicationUser = new MockApplicationUser(USER);

        when(jiraAuthenticationContext.getUser()).thenReturn(applicationUser);
        when(userPreferencesManager.getExtendedPreferences(applicationUser)).thenReturn(jiraUserPreferences);
    }

    @Test
    public void gettingTheValueForANullPreferenceThrowsAWebApplicationException()
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.getPreference(null);
    }

    @Test
    public void gettingTheValueForAnEmptyPreferenceThrowsAWebApplicationException()
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.getPreference("");
    }

    @Test
    public void gettingNonexistentPreferenceThrowsAWebApplicationException()
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));
        expectedException.expect(getExceptionMessageContainsMatcher(NONEXISTENT_KEY));

        preferencesResource.getPreference(NONEXISTENT_KEY);
    }

    @Test
    public void settingTheValueForANullPreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.setPreference(null, SOME_VALUE);
    }

    @Test
    public void settingTheValueForAnEmptyPreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.setPreference("", SOME_VALUE);
    }

    @Test
    public void settingANullValueForThePreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(VALUE));

        preferencesResource.setPreference(SOME_KEY, null);
    }

    @Test
    public void settingAnEmptyValueForThePreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(VALUE));

        preferencesResource.setPreference(SOME_KEY, "");
    }


    @Test
    public void removingANullPreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.removePreference(null);
    }

    @Test
    public void removingAnEmptyPreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));

        preferencesResource.removePreference("");
    }

    @Test
    public void removingNonexistentPreferenceThrowsAWebApplicationException() throws AtlassianCoreException
    {
        expectedException.expect(WebApplicationException.class);
        expectedException.expect(getExceptionMessageContainsMatcher(KEY));
        expectedException.expect(getExceptionMessageContainsMatcher(NONEXISTENT_KEY));

        preferencesResource.removePreference(NONEXISTENT_KEY);
    }


    private Matcher<WebApplicationException> getExceptionMessageContainsMatcher(final String messagePart)
    {
        return ErrorMatchers.withTransformed(containsString(messagePart), new Function<WebApplicationException, String>()
        {
            @Override
            public String apply(@Nullable final WebApplicationException input)
            {
                ErrorCollection errorCollection = (ErrorCollection) input.getResponse().getEntity();
                Collection<String> messages = errorCollection.getErrorMessages();
                if (messages.isEmpty())
                {
                    return "";
                }

                return messages.iterator().next();
            }
        });
    }


}
