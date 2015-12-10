package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.List;

class FieldScreenRendererImpl extends AbstractFieldScreenRenderer
{
    private final List<FieldScreenRenderTab> fieldScreenRenderTabs;
    private final FieldLayout fieldLayout;

    FieldScreenRendererImpl(List<FieldScreenRenderTab> fieldScreenRenderTabs, FieldLayout fieldLayout)
    {
        this.fieldScreenRenderTabs = fieldScreenRenderTabs;
        this.fieldLayout = fieldLayout;
    }

    public List<FieldScreenRenderTab> getFieldScreenRenderTabs()
    {
        return fieldScreenRenderTabs;
    }

    public FieldLayout getFieldLayout()
    {
        return fieldLayout;
    }
}
