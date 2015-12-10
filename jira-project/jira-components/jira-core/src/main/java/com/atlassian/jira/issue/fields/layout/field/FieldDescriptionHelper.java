package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;

/**
 * Render the system field description using wiki renderer for On Demand
 *
 * @since v5.1.1
 */
public class FieldDescriptionHelper
{
    private final FeatureManager featureManager;
    private final RendererManager rendererManager;


    public FieldDescriptionHelper(RendererManager rendererManager, FeatureManager featureManager) {
        this.rendererManager = rendererManager;
        this.featureManager = featureManager;
    }

    public String getDescription(String fieldDescription)
    {
        String description = fieldDescription;
        if (featureManager.isOnDemand())
        {
            description = rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, fieldDescription, null);
        }
        return description;
    }
}
