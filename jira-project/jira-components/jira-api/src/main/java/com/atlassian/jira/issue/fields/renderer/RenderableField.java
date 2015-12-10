package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;

/**
 * This interface defines the requirements for a field to be used in the Rendering system.
 */
@PublicApi
public interface RenderableField
{
    /**
     * Returns the identifier for this RenderableField.
     * @return the identifier for this RenderableField.
     */
    public String getId();

    /**
     * Gets the value stored for this field on the provided issue.
     * @param issue identifies the issue that will contain the value for this field.
     * @return the value stored on this issue for this field, null if not applicable.
     */
    public String getValueFromIssue(Issue issue);

    /**
     * Defines if a field determines itself as renderable. This is needed because of the way that customfields
     * are implemented. Since all the real work is done in CustomFieldTypes, if we want to treat system fields
     * and custom fields the same way in the renderers then all custom fields must implement this interface. We
     * therefore provide this method so that the CustomFieldImpl can delegate to the CustomFieldTypes to determine
     * if a given custom field is renderable.
     * @return true if the field is renderable, false otherwise.
     */
    public boolean isRenderable();
}
