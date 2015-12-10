package com.atlassian.jira.template.soy;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

public class SoyTemplateRendererProviderImpl implements SoyTemplateRendererProvider
{
    @Override
    public SoyTemplateRenderer getRenderer()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(SoyTemplateRenderer.class);
    }
}
