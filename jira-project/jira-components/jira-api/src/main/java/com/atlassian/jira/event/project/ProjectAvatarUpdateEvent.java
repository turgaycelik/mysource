package com.atlassian.jira.event.project;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.Project;

/**
 * Event published when avatar of a project changes
 * @since 6.3
 */
public class ProjectAvatarUpdateEvent {

    private final Project project;
    private final Long newAvatarId;

    @Internal
    public ProjectAvatarUpdateEvent(@Nonnull final Project project, @Nonnull final Long newAvatarId) {
        this.project = project;
        this.newAvatarId = newAvatarId;
    }

    public Project getProject() {
        return project;
    }

    public Long getOldAvatarId() {
        return project.getAvatar().getId();
    }

    public Long getNewAvatarId() {
        return newAvatarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectAvatarUpdateEvent that = (ProjectAvatarUpdateEvent) o;

        if (newAvatarId != null ? !newAvatarId.equals(that.newAvatarId) : that.newAvatarId != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (newAvatarId != null ? newAvatarId.hashCode() : 0);
        return result;
    }
}
