package com.atlassian.jira.project.renderer;

import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @since 5.0.5
 */
public class WikiMarkupProjectDescriptionRenderer implements ProjectDescriptionRenderer
{
    static final ImmutableMap<String,String> DEFAULT_RENDER_PARAMETERS = ImmutableMap.of(
            "rows", "10",
            "cols", "60",
            "wrap", "virtual",
            "class", "long-field"
    );
    static final String PROJECT_DESCRIPTION_FIELD_ID = "description";
    static final String PROJECT_DESCRIPTION_FIELD_NAME = "description";
    static final boolean NO_SINGLE_LINE = false;

    private final RendererManager rendererManager;

    public WikiMarkupProjectDescriptionRenderer(@Nonnull final RendererManager rendererManager) {
        this.rendererManager = rendererManager;
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

    @Nonnull
    @Override
    public String getViewHtml(@Nonnull final String description)
    {
        return getRenderer().render(description, null);
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final String description)
    {
        final JiraRendererModuleDescriptor rendererDescriptor = getRenderer().getDescriptor();
        final Map<String, String> renderParams = Maps.newHashMap(DEFAULT_RENDER_PARAMETERS);

        return rendererDescriptor.getEditVM(description, null, AtlassianWikiRenderer.RENDERER_TYPE,
                PROJECT_DESCRIPTION_FIELD_ID, PROJECT_DESCRIPTION_FIELD_NAME, renderParams, NO_SINGLE_LINE);
    }

    @Nonnull
    @Override
    public String getDescriptionI18nKey()
    {
        return "admin.addproject.description.wikimarkup";
    }

    @Override
    public boolean isUseWikiMarkup()
    {
        return true;
    }

    @Nonnull
    private JiraRendererPlugin getRenderer()
    {
        final JiraRendererPlugin renderer = rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE);
        if (renderer == null)
        {
            throw new InfrastructureException("wikimarkup renderer not found, but it is required to render the project description");
        }
        return renderer;
    }
}
