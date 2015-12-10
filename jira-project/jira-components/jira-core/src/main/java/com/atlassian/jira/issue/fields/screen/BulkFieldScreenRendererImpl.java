package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Collections;
import java.util.List;

public class BulkFieldScreenRendererImpl extends AbstractFieldScreenRenderer
{
    private final List<FieldScreenRenderTab> fieldScreenRenderTabs;

    BulkFieldScreenRendererImpl(final List<FieldScreenRenderTab> fieldScreenRenderTabs)
    {
        this.fieldScreenRenderTabs = Collections.unmodifiableList(fieldScreenRenderTabs);
    }

    public List<FieldScreenRenderTab> getFieldScreenRenderTabs()
    {
        return fieldScreenRenderTabs;
    }

    // INTERFACE METHODS NOT USED --------------------------------------------------------------------------------------

    public FieldLayout getFieldLayout()
    {
        throw new UnsupportedOperationException("This method is not available for BulkFieldScreenRenderer");
    }
}
