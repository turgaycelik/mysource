package com.atlassian.jira.avatar;

import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

/**
 * Represents a user or project avatar within our "network" of federated JIRA servers.
 *
 * <p>Normally this will represent an avatar coming from one of the JIRA servers that we have an app link with.
 * However, because it can be useful to mix both local and remote users, one can construct an instance
 * of this class that represents a local avatar.
 * </p>
 *
 * <p>This class is considered experimental while RemoteProjectService is still being developed.</p>
 */
@ExperimentalApi
@Immutable
public class RemoteAvatar
{
    private final String url16x16;
    private final String url24x24;
    private final String url32x32;
    private final String url48x48;

    public RemoteAvatar(final String url16x16, final String url24x24, final String url32x32, final String url48x48)
    {
        this.url16x16 = url16x16;
        this.url24x24 = url24x24;
        this.url32x32 = url32x32;
        this.url48x48 = url48x48;
    }

    @HtmlSafe
    public String getUrl16x16()
    {
        return url16x16;
    }

    @HtmlSafe
    public String getUrl24x24()
    {
        return url24x24;
    }

    @HtmlSafe
    public String getUrl32x32()
    {
        return url32x32;
    }

    @HtmlSafe
    public String getUrl48x48()
    {
        return url48x48;
    }

    /**
     * Build an Avatar representation for a remote object.
     *
     * @param avatarUrls the "avatarUrls" JSON from the project or user.
     * @return the RemoteAvatar representation of this JSON data.
     *
     * @throws JSONException if the JSON is missing any of the required fields.
     */
    public static RemoteAvatar from(final JSONObject avatarUrls) throws JSONException
    {
        return new RemoteAvatar(
                avatarUrls.getString("16x16"),
                avatarUrls.getString("24x24"),
                avatarUrls.getString("32x32"),
                avatarUrls.getString("48x48")
                );
    }

    /**
     * Build an Avatar representation for a local project avatar.
     *
     * @param project the project
     * @return the RemoteAvatar representation of this local project avatar.
     */
    public static RemoteAvatar from(final Project project)
    {
        Long avatarId = project.getAvatar().getId();
        if (avatarId == null)
            return null;

        String baseUrl = ComponentAccessor.getComponent(JiraBaseUrls.class).baseUrl() + "/secure/projectavatar?avatarId=" + avatarId;
        return new RemoteAvatar(baseUrl + "&size=xsmall",
                baseUrl + "&size=small",
                baseUrl + "&size=medium",
                baseUrl
                );
    }

    /**
     * Build an Avatar representation for a local user avatar.
     *
     * @param user the user
     * @return the RemoteAvatar representation of this local user avatar.
     */
    public static RemoteAvatar from(final ApplicationUser user)
    {
        String baseUrl = ComponentAccessor.getComponent(JiraBaseUrls.class).baseUrl() + "/secure/useravatar?ownerId=" + user.getKey();
        return new RemoteAvatar(baseUrl + "&size=xsmall",
                baseUrl + "&size=small",
                baseUrl + "&size=medium",
                baseUrl
        );
    }
}
