package com.atlassian.jira.web.action.admin.issuefields.configuration;

import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Responsible for rendering the user interface to add a new field configuration to a JIRA instance.
 *
 * @since v5.0.1
 */
@WebSudoRequired
public class AddFieldConfiguration extends JiraWebActionSupport
{
    private String fieldLayoutName;
    private String fieldLayoutDescription;

    private final FieldLayoutManager fieldLayoutManager;
    private List<EditableFieldLayout> editableFieldLayouts;

    public AddFieldConfiguration(final FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    /**
     * Renders the dialog to input the values for a new field configuration.
     * 
     * @return {@link webwork.action.Action#INPUT}
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception
    {
        return INPUT;
    }

    /**
     * Handles the request to create a new field configuration submitted from the add new field configuration dialog.
     * 
     * On success, we redirect to the view field configurations page.
     * 
     * On error, we return the user to the dialog.
     * 
     * @return {@link webwork.action.Action#SUCCESS} on success, {@link webwork.action.Action#ERROR} on validation
     * errors.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isBlank(getFieldLayoutName()))
        {
            addError("fieldLayoutName", getText("admin.errors.empty.field.configuration.name"));
            return ERROR;
        }

        if (!invalidInput())
        {
            // Ensure no field layout with this name exists
            for (final FieldLayout fieldLayout : getFieldLayouts())
            {
                if (getFieldLayoutName().equals(fieldLayout.getName()))
                {
                    addError("fieldLayoutName", getText("admin.errors.fieldlayout.name.exists"));
                    return ERROR;
                }
            }
        }

        final EditableDefaultFieldLayout editableDefaultFieldLayout = fieldLayoutManager.getEditableDefaultFieldLayout();
        // Create a field layout with the same field properties as the default field layout
        final EditableFieldLayout editableFieldLayout = new EditableFieldLayoutImpl(null, editableDefaultFieldLayout.getFieldLayoutItems());
        editableFieldLayout.setName(getFieldLayoutName());
        editableFieldLayout.setDescription(getFieldLayoutDescription());
        final EditableFieldLayout newLayout = fieldLayoutManager.storeAndReturnEditableFieldLayout(editableFieldLayout);
        return returnCompleteWithInlineRedirect(format("ConfigureFieldLayout!default.jspa?id=%d", newLayout.getId()));
    }

    public List<EditableFieldLayout> getFieldLayouts()
    {
        if (editableFieldLayouts == null)
        {
            editableFieldLayouts = fieldLayoutManager.getEditableFieldLayouts();
        }

        return editableFieldLayouts;
    }

    public String getFieldLayoutName()
    {
        return fieldLayoutName;
    }

    public void setFieldLayoutName(final String fieldLayoutName)
    {
        this.fieldLayoutName = fieldLayoutName;
    }

    public String getFieldLayoutDescription()
    {
        return fieldLayoutDescription;
    }

    public void setFieldLayoutDescription(final String fieldLayoutDescription)
    {
        this.fieldLayoutDescription = fieldLayoutDescription;
    }
}
