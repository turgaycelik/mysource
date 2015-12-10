package com.atlassian.jira.user;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.avatar.RemoteAvatar;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

/**
 * Represents a JIRA User within our "network" of federated JIRA servers.
 *
 * <p>Normally this will represent a user coming from one of the JIRA servers that we have an app link with.
 * However, because it can be useful to mix both local and remote users, one can construct an instance
 * of this class that represents a local avatar.
 * </p>
 *
 * <p>This class is considered experimental while RemoteProjectService is still being developed.</p>
 */
@ExperimentalApi
@Immutable
public class RemoteUser
{
    private final String username;
    private final String displayName;
    private final boolean active;
    private final RemoteAvatar avatar;

    public RemoteUser(final String username, final String displayName, final boolean active, final RemoteAvatar avatar) {
        this.username = username;
        this.displayName = displayName;
        this.active = active;
        this.avatar = avatar;
    }

    @Nonnull
    public String getUsername()
    {
        return username;
    }

    @Nonnull
    public String getDisplayName()
    {
        return displayName;
    }

    @Nonnull
    public boolean isActive()
    {
        return active;
    }

    /**
     * Returns the user's avatar representation.
     *
     * @return the user's avatar representation.
     */
    public RemoteAvatar getAvatar()
    {
        return avatar;
    }

    public static RemoteUser from(final JSONObject user) throws JSONException
    {
        //        "self": "http://www.example.com/jira/rest/api/2/user?username=fred",
        //            "name": "fred",
        //            "avatarUrls": {
        //                "24x24": "http://www.example.com/jira/secure/useravatar?size=small&ownerId=fred",
        //                "16x16": "http://www.example.com/jira/secure/useravatar?size=xsmall&ownerId=fred",
        //                "32x32": "http://www.example.com/jira/secure/useravatar?size=medium&ownerId=fred",
        //                "48x48": "http://www.example.com/jira/secure/useravatar?size=large&ownerId=fred"
        //              },
        //            "displayName": "Fred F. User",
        //            "active": false
        return new RemoteUser(
                user.getString("name"),
                user.getString("displayName"),
                user.getBoolean("active"),
                RemoteAvatar.from(user.getJSONObject("avatarUrls"))
        );
    }

    public static RemoteUser from(final ApplicationUser user)
    {
        return new RemoteUser(
                user.getUsername(),
                user.getDisplayName(),
                user.isActive(),
                RemoteAvatar.from(user)
        );
    }
}
