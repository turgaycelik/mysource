package com.atlassian.jira.project.renderer;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.project.Project;

/**
 * Renders the project description either as wiki markup or returns the plain value (in case HTML is allowed).
 *
 * @since 5.0.5
 */
public class ProjectDescriptionRendererImpl implements ProjectDescriptionRenderer
{
    private final RendererManager rendererManager;
    private final ApplicationProperties applicationProperties;
    private final WikiMarkupProjectDescriptionRenderer wikiMarkupProjectDescriptionRenderer;
    private final FullHtmlProjectDescriptionRenderer fullHtmlProjectDescriptionRenderer;

    public ProjectDescriptionRendererImpl(@Nonnull final RendererManager rendererManager, @Nonnull final ApplicationProperties applicationProperties)
    {
        this.rendererManager = rendererManager;
        this.applicationProperties = applicationProperties;

        this.wikiMarkupProjectDescriptionRenderer = new WikiMarkupProjectDescriptionRenderer(rendererManager);
        this.fullHtmlProjectDescriptionRenderer = new FullHtmlProjectDescriptionRenderer();
    }

    @Nonnull
    @Override
    public String getViewHtml(@Nonnull final Project project)
    {
        return getViewHtml(project.getDescription());
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final Project project)
    {
        return getEditHtml(project.getDescription());
    }

    @Override
    @Nonnull
    public String getViewHtml(@Nonnull final String description)
    {
        return renderer().getViewHtml(description);
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final String description)
    {
        return renderer().getEditHtml(description);
    }

    @Override
    @Nonnull
    public String getDescriptionI18nKey()
    {
        return renderer().getDescriptionI18nKey();
    }

    @Nonnull
    private ProjectDescriptionRenderer renderer()
    {
        return isUseWikiMarkup() ? wikiMarkupProjectDescriptionRenderer : fullHtmlProjectDescriptionRenderer;
    }

    /**
     * @return true if we should use wiki markup in the project description field
     */
    @Override
    public boolean isUseWikiMarkup()
    {
        return !applicationProperties.getOption(APKeys.JIRA_OPTION_PROJECT_DESCRIPTION_HTML_ENABLED);
    }

}
