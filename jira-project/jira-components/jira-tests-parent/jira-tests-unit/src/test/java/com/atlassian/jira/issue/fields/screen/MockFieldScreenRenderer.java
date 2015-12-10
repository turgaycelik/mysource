package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple mock implementation of {@link com.atlassian.jira.issue.fields.screen.AbstractFieldScreenRenderer}.
 *
 * @since v4.1
 */
public class MockFieldScreenRenderer extends AbstractFieldScreenRenderer
{
    private List<FieldScreenRenderTab> tabs;
    private FieldLayout layout;

    public MockFieldScreenRenderer(final List<? extends FieldScreenRenderTab> tabs, final FieldLayout layout)
    {
        setFieldScreenRenderTabs(tabs);
        setFieldLayout(layout);
    }

    public MockFieldScreenRenderer()
    {
    }

    public List<FieldScreenRenderTab> getFieldScreenRenderTabs()
    {
        return tabs;
    }

    public FieldLayout getFieldLayout()
    {
        return layout;
    }

    public MockFieldScreenRenderer setFieldScreenRenderTabs(final List<? extends FieldScreenRenderTab> tabs)
    {
        this.tabs = new ArrayList<FieldScreenRenderTab>(tabs);
        return this;
    }

    public MockFieldScreenRenderer addFieldScreenRenderTab(FieldScreenRenderTab tab)
    {
        if (this.tabs == null)
        {
            this.tabs = new ArrayList<FieldScreenRenderTab>();
        }
        this.tabs.add(tab);
        return this;
    }

    public MockFieldScreenRendererTab addFieldScreenRendererTab()
    {
        final MockFieldScreenRendererTab tab = new MockFieldScreenRendererTab();
        addFieldScreenRenderTab(tab);
        return tab;
    }

    public MockFieldScreenRenderer setFieldLayout(final FieldLayout layout)
    {
        this.layout = layout;
        return this;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
