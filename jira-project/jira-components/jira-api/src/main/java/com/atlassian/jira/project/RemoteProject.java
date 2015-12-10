package com.atlassian.jira.project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.avatar.RemoteAvatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.user.RemoteUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

/**
 * Represents a JIRA Project within our "network" of federated JIRA servers.
 *
 * <p>Normally this will represent a project coming from one of the JIRA servers that we have an app link with.
 * However, because it can be useful to create a list with both local and remote projects, one can construct an instance
 * of this class that represents a local project.
 * </p>
 *
 * <p>This class is considered experimental while RemoteProjectService is still being developed.</p>
 */
@ExperimentalApi
@Immutable
public final class RemoteProject
{
    private final long id;
    private final String key;
    private final String name;
    private final String description;
    private final RemoteUser leadUser;
    private final String url;
    private final ApplicationLink applicationLink;
    private final RemoteAvatar avatar;

    public RemoteProject(final long id, final String key, final String name, final String description, final RemoteUser leadUser,
            final String url, final ApplicationLink applicationLink, final RemoteAvatar avatar)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.leadUser = leadUser;
        this.url = url;
        this.applicationLink = applicationLink;
        this.avatar = avatar;
    }

    public long getId()
    {
        return id;
    }

    @Nonnull
    public String getKey()
    {
        return key;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * Returns the description for the underlying project, or null if the description was not supplied in the JSON response.
     * <p/>
     * An empty String is returned if the project has a blank description.
     * A null return value indicates that the description was not returned over the wire, and should be treated as an
     * "unknown" value.
     *
     * @return the description for the underlying project, or null if the description was not supplied in the JSON response.
     */
    @Nullable
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the Project Lead for this project.
     * <p/>
     * In the REST API for JIRA 6.1, this value is returned when getting a single project, but not when getting all projects, so this
     * value can sometimes be null even if the underlying project has a lead. Developers should treat a null value as
     * "unknown".
     *
     * @return the Project Lead for this project.
     */
    @Nullable
    public RemoteUser getLeadUser()
    {
        return leadUser;
    }

    /**
     * Returns the configured "URL" value for this project.
     * <p/>
     * In the REST API for JIRA 6.1, this value is returned when getting a single project, but not when getting all projects, so this
     * value can sometimes be null even if the underlying project has a URL specified.
     * Consumers should treat a null value as "unknown".
     *
     * @return the configured "URL" value for this project, or null if not known.
     */
    @Nullable
    public String getUrl()
    {
        return url;
    }

    /**
     * Returns the AppLink for the server that this remote project lives on.
     * <p/>
     * Will return null if the project is representing a local project on this server.
     *
     * @return the AppLink for the server that this remote project lives on, or null for a local project.
     */
    @Nullable
    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    @Nonnull
    public RemoteAvatar getAvatar()
    {
        return avatar;
    }

    /**
     * Returns the URL for the "Browse Project" page of this project.
     *
     * @return the URL for the "Browse Project" page of this project.
     */
    @Nonnull
    public String getBrowseProjectUrl()
    {
        return getServerBaseUrl() + "/browse/" + key;
    }

    /**
     * Returns the Base URL for the server that this project lives on.
     *
     * @return the Base URL for the server that this project lives on.
     */
    @Nonnull
    public String getServerBaseUrl()
    {
        if (applicationLink == null)
        {
            return ComponentAccessor.getComponent(JiraBaseUrls.class).baseUrl();
        }
        else
        {
            return applicationLink.getDisplayUrl().toString();
        }
    }

    /**
     * Build a Network project instance from a local Project.
     *
     * @param project the local project
     * @return a NetworkProject instance representing the given local project.
     */
    @Nonnull
    public static RemoteProject from(final Project project)
    {
        return new RemoteProject(project.getId(), project.getKey(), project.getName(), project.getDescription(), RemoteUser.from(project.getProjectLead()), project.getUrl(), null, RemoteAvatar.from(project));
    }

    @Nonnull
    public static RemoteProject from(ApplicationLink applicationLink, final JSONObject jsonObject) throws JSONException
    {
        Builder builder = new Builder();
        builder.applicationLink(applicationLink);
        builder.id(jsonObject.getLong("id"));
        builder.key(jsonObject.getString("key"));
        builder.name(jsonObject.getString("name"));
        if (jsonObject.has("description"))
            builder.description(jsonObject.getString("description"));
        if (jsonObject.has("url"))
            builder.url(jsonObject.getString("url"));
        if (jsonObject.has("lead"))
            builder.leadUser(RemoteUser.from(jsonObject.getJSONObject("lead")));
        if (jsonObject.has("avatarUrls"))
            builder.avatar(RemoteAvatar.from(jsonObject.getJSONObject("avatarUrls")));
        return builder.build();
    }

    public static final class Builder
    {
        private long id;
        private String key;
        private String name;
        private String description;
        private RemoteUser leadUser;
        private String url;
        private ApplicationLink applicationLink;
        private RemoteAvatar avatar;

        public Builder id(final long id)
        {
            this.id = id;
            return this;
        }

        public Builder key(final String key)
        {
            this.key = key;
            return this;
        }

        public Builder applicationLink(final ApplicationLink applicationLink)
        {
            this.applicationLink = applicationLink;
            return this;
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder leadUser(final RemoteUser leadUser)
        {
            this.leadUser = leadUser;
            return this;
        }

        public Builder url(final String url)
        {
            this.url = url;
            return this;
        }

        public Builder avatar(final RemoteAvatar avatar)
        {
            this.avatar = avatar;
            return this;
        }

        public RemoteProject build()
        {
            return new RemoteProject(id, key, name, description, leadUser, url, applicationLink, avatar);
        }
    }
}
