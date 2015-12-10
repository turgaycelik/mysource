/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 10, 2004
 * Time: 12:29:39 PM
 */
package com.atlassian.jira.project.version;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectConstant;
import com.atlassian.jira.util.NamedWithDescription;

import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.Date;

@PublicApi
public interface Version extends OfBizValueWrapper, ProjectConstant, NamedWithDescription
{
    /**
     * Returns Project as a GenericValue.
     * @return Project as a GenericValue.
     *
     * @deprecated Please use {@link #getProjectObject()}. Since v4.0
     */
    GenericValue getProject();

    /**
     * Returns the ID of the project that this version belongs to.
     *
     * @return the ID of the project that this version belongs to.
     * @since v5.2
     */
    Long getProjectId();

    /**
     * Returns project this version relates to.
     *
     * @return project domain object
     * @since v3.10
     */
    Project getProjectObject();

    Long getId();

    String getName();

    void setName(String name);

    @Nullable String getDescription();

    void setDescription(@Nullable String description);

    Long getSequence();

    void setSequence(Long sequence);

    boolean isArchived();

    void setArchived(boolean archived);

    boolean isReleased();

    void setReleased(boolean released);

    @Nullable Date getReleaseDate();

    void setReleaseDate(@Nullable Date releasedate);

    /**
     * Returns the start date of the version
     *
     * @return The start date of the version
     *
     * @since v6.0
     */
    @Nullable Date getStartDate();

    /**
     * Sets the start date of the version
     *
     * @param startDate The start date of the version
     *
     * @since v6.0
     */
    void setStartDate(@Nullable Date startDate);

    /**
     * @return a clone of this version including a cloned generic value
     * @since v7.0
     */
    Version clone();
}