package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;

import java.util.List;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate User Profiles.
 *
 * See UserProfileBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class UserProfileControl extends BackdoorControl<UserProfileControl>
{
    public UserProfileControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Changes the given user's email format to the one supplied.
     *
     * @param username the user to change the email format for
     * @param format either "html" or "text"
     */
    public void changeUserNotificationType(String username, String format)
    {
        get(createResource().path("userProfile/notificationType/set")
                .queryParam("username", username)
                .queryParam("format", format));
    }

    /**
     * Changes the given user's language to the supplied language
     *
     * @param username the user to change the email format for
     * @param language the language the user's profile will be updated to
     */
    public void changeUserLanguage(String username, String language)
    {
        (createResource().path("userProfile/language")
                .queryParam("username", username)
                .queryParam("language", language)).put();
    }

    /**
     * Changes the given user's autowatch value.
     *
     * @param autoWatchValue autowatch value. Can pass null, no autowatch preference.
     * @param username the user to change the autowatch value.
     */
    public void changeUserAutoWatch(final Boolean autoWatchValue, final String username)
    {
        (createResource().path("userProfile/autowatch")
            .queryParam("username", username)
            .queryParam("autowatchvalue", autoWatchValue != null ? autoWatchValue.toString() : ""))
            .put();
    }

	public String getAvatarUrl(final String username) {
		return createResource().path("userProfile/avatar/url")
				.queryParam("username", username)
				.get(String.class);
	}

    public List<String> getCustomAvatarIds(final String username)
    {
        return createResource().path("userProfile/avatar/customIds")
                .queryParam("username", username)
                .get(new GenericType<List<String>>(){});
    }

}
