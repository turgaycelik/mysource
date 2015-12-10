package com.atlassian.jira.project;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used to produce Project objects.
 */
public interface ProjectFactory
{
    /**
     * Returns a project object that is backed by the given generic value.
     * @param projectGV project generic value
     * @return existing project or null if project does not exist
     */
    @Nullable
    public Project getProject(@Nullable GenericValue projectGV);

    /**
     * Returns the collection of Project objects that are backed by the
     * GenericValue objects in the given collection.
     * @param projectGVs GenericValues of projects to return.
     * @return the Collection of existing projects, possibly empty, never null.
     */
    @Nonnull
    public List<Project> getProjects(@Nonnull Collection<GenericValue> projectGVs);
}
