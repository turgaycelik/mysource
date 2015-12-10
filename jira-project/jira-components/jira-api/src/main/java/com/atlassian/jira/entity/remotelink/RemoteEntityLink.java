package com.atlassian.jira.entity.remotelink;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Common abstract class for remote entity links.
 *
 * @since v6.1
 */
@ExperimentalApi
public interface RemoteEntityLink<E>
{
    /**
     * The name of the JSON object field that may be used to suggest the global ID
     * to use when creating a remote entity link.  The services that create remote
     * entity links accept a global ID and the raw JSON value meaningful to the link
     * service.  If the global ID is not specified directly but the JSON value is an
     * object containing a text field by this name ("{@value}"), that value is
     * used as the global ID.  If neither method of supplying the global ID is
     * used, then a random global ID will be generated for the link, instead.
     * <p/>
     * See {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService#validatePut(
     * com.atlassian.jira.user.ApplicationUser, Long, String, String) RemoteVersionLinkService}
     * for an example of this.
     */
    public static final String GLOBAL_ID = "globalId";

    /**
     * Returns the local entity to which the remote entity link is associated.  For example, if this is a
     * {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLink RemoteVersionLink}, then
     * this will return a {@link com.atlassian.jira.project.version.Version Version}.
     * @return the local entity to which the remote entity link is associated.
     */
    E getEntity();

    /**
     * Returns the entity ID for the local entity to which the remote entity link is associated.  For example,
     * if this is a {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLink RemoteVersionLink},
     * then this will be a {@link com.atlassian.jira.project.version.Version#getId() version ID}.
     *
     * @return the entity ID for the local entity to which the remote entity link is associated.
     */
    @Nonnull Long getEntityId();

    /**
     * Returns the global ID for the remote entity link.
     * @return the global ID for the remote entity link.
     */
    @Nonnull String getGlobalId();

    /**
     * Returns the raw JSON value stored for this remote entity link.
     * @return the raw JSON value stored for this remote entity link.
     */
    @Nonnull String getJsonString();

    /**
     * Returns the target object title for the remote entity link, as extracted from the raw JSON.
     * @return the target object title for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getTitle();

    /**
     * Returns the target object summary for the remote entity link, as extracted from the raw JSON.
     * @return the target object summary for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getSummary();

    /**
     * Returns the target object URL for the remote entity link, as extracted from the raw JSON.
     * @return the target object URL for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getUrl();

    /**
     * Returns the target object icon URL for the remote entity link, as extracted from the raw JSON.
     * @return the target object icon URL for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getIconUrl();

    /**
     * Returns the target object icon title for the remote entity link, as extracted from the raw JSON.
     * @return the target object icon title for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getIconTitle();

    /**
     * Returns the application name for the remote entity link, as extracted from the raw JSON.
     * @return the application name for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getApplicationName();

    /**
     * Returns the application type for the remote entity link, as extracted from the raw JSON.
     * @return the application type for the remote entity link, or {@code null} if it is not defined
     */
    @Nullable String getApplicationType();
}
