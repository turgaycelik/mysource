package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.resolution.Resolution;

import java.util.List;

/**
 * Manager for {@link Resolution}s.
 *
 * @since v5.0
 */
@PublicApi
public interface ResolutionManager
{
    /**
     * Adds a new resolution. The new resolution must have a unique name.
     *
     * @param name resolution name. Cannot be null or blank.
     * @param description the resolution description. Can be null or blank.
     *
     * @return the new {@link Resolution}.
     */
    Resolution createResolution(String name, String description);

    /**
     * Edits an existing resolution.
     *
     * @param resolution resolution to edit.
     * @param name the new name.
     * @param description the new description
     */
    void editResolution(Resolution resolution, String name, String description);

    /**
     * Return all resolutions. The list is ordered by the sequence.
     * The order can be modified by calling {@link #moveResolutionDown(String)} or {@link #moveResolutionUp(String)}.
     *
     * @return a list of {@link Resolution}s
     */
    List<Resolution> getResolutions();

    /**
     * Removes a resolution with the specified id. When removing a resolution all issues which have this resolution will have to
     * be migrated to use a different resultion specified by the second argument.
     *
     * @param resolutionId  resolution to remove.
     * @param newResolutionId new resolution for issues which have the resolution which has been removed. Cannot be null.
     */
    void removeResolution(String resolutionId, String newResolutionId);

    /**
     * Returns the resolution with the specified name. It is case insensitive.
     *
     * @param name the name of the resolution.
     *
     * @return a {@link Resolution} with the specified name.
     */
    Resolution getResolutionByName(String name);

    /**
     * Returns a resolution with the specified id.
     *
     * @param id resolution id.
     * @return a {@link Resolution} with the specified id or null if no resolution whith this id exists.
     */
    Resolution getResolution(String id);

    /**
     * Move the resolution up in the order.
     *
     * @param id id of the resolution.
     */
    void moveResolutionUp(String id);

    /**
     * Move the resolution down in the order.
     *
     * @param id id of the resolution.
     */
    void moveResolutionDown(String id);

    /**
     * Sets the default resolution.
     * <p/>
     * You can pass null to clear the default.
     *
     * @param id resolution id or null.
     */
    void setDefaultResolution(String id);

    /**
     * Returns the default resolution.
     * <p/>
     * Will return null if no default is set.
     * @return the default resolution.
     */
    Resolution getDefaultResolution();

}
