package com.atlassian.jira.template.soy;

import com.atlassian.soy.renderer.SoyTemplateRenderer;

/**
 * A Pico-injectable component that provides a {@link SoyTemplateRenderer}.
 *
 * @since v5.0
 */
public interface SoyTemplateRendererProvider
{
    SoyTemplateRenderer getRenderer();
}
