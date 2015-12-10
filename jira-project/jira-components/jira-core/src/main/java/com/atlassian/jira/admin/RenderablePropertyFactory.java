package com.atlassian.jira.admin;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.velocity.VelocityManager;

import javax.annotation.Nullable;

/**
 * Factory for {@code RenderableApplicationProperty} instances.
 *
 * @since v5.0.7
 */
@Internal
public class RenderablePropertyFactory
{
    private final ApplicationProperties applicationProperties;
    private final FeatureManager featureManager;
    private final VelocityManager jiraVelocityManager;
    private final RendererManager rendererManager;

    public RenderablePropertyFactory(ApplicationProperties applicationProperties, FeatureManager featureManager, VelocityManager jiraVelocityManager, RendererManager rendererManager)
    {
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
        this.jiraVelocityManager = jiraVelocityManager;
        this.rendererManager = rendererManager;
    }

    /**
     * Creates a new RenderablePropertyImpl instance that uses the given {@code persister} and {@code descriptions}.
     *
     * @param persister a PropertyPersister
     * @param propertyDescription a PropertyDescriptions (may be null)
     * @return a new RenderablePropertyImpl
     * @since v5.0.7
     */
    public RenderablePropertyImpl create(PropertyPersister persister, @Nullable PropertyDescriptions propertyDescription)
    {
        return new RenderablePropertyImpl(applicationProperties, featureManager, jiraVelocityManager, rendererManager, persister, propertyDescription);
    }
}
