package com.atlassian.jira.web.bean;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Strings;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ProjectDescriptionRendererBean
{
    private final ProjectDescriptionRenderer renderer;

    public ProjectDescriptionRendererBean(@Nonnull final ProjectDescriptionRenderer renderer)
    {
        this.renderer = Assertions.notNull(renderer);
    }

    @SuppressWarnings ("UnusedDeclaration")
    public ProjectDescriptionRendererBean()
    {
        this(ComponentAccessor.getComponent(ProjectDescriptionRenderer.class));
    }

    public String getViewHtml(@Nonnull final GenericValue genericValue)
    {
        notNull("generic value must not be null", genericValue);
        final ProjectImpl project = new ProjectImpl(genericValue);
        return getViewHtml(project);
    }

    @Nonnull
    public String getViewHtml(@Nonnull final Project project)
    {
        notNull("project must not be null", project);
        return renderDescriptionAsHtml(project);
    }

    @Nonnull
    private String renderDescriptionAsHtml(@Nonnull final Project project)
    {
        return Strings.nullToEmpty(renderer.getViewHtml(project.getDescription()));
    }

}
