package com.atlassian.jira.web.action.admin.issuefields.configuration.schemes;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Responsible for rendering the user interface to add a new field configuration scheme to a JIRA instance.
 *
 * @since v5.0.1
 */
@WebSudoRequired
public class AddFieldConfigurationScheme extends JiraWebActionSupport
{
    private String fieldLayoutSchemeName;
    private String fieldLayoutSchemeDescription;

    private final FieldLayoutManager fieldLayoutManager;

    public AddFieldConfigurationScheme(final FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    /**
     * Renders the dialog to input the values for a new field configuration scheme.
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
     * Handles the request to create a new field configuration scheme as submitted from the dialog.
     *
     * On success, we redirect to the view field configurations schemes page.
     *
     * On error, we return the user to the dialog.
     *
     * @return redirect to {@link com.atlassian.jira.web.action.admin.issuefields.enterprise.ViewSchemes} on sucess,
     * {@link webwork.action.Action#ERROR} if there are validation errors.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isBlank(getFieldLayoutSchemeName()))
        {
            addError("fieldLayoutSchemeName", getText("admin.errors.empty.field.configuration.scheme.name"));
            return ERROR;
        }
        else if (fieldLayoutManager.fieldConfigurationSchemeExists(getFieldLayoutSchemeName()))
        {
            addError("fieldLayoutSchemeName", getText("admin.errors.fieldlayout.scheme.name.exists"));
            return ERROR;
        }

        final FieldLayoutScheme fieldLayoutScheme = fieldLayoutManager.createFieldLayoutScheme(getFieldLayoutSchemeName(), getFieldLayoutSchemeDescription());

        return returnCompleteWithInlineRedirect(format("ConfigureFieldLayoutScheme!default.jspa?id=%d", fieldLayoutScheme.getId()));
    }

    public String getFieldLayoutSchemeName()
    {
        return fieldLayoutSchemeName;
    }

    public void setFieldLayoutSchemeName(final String fieldLayoutSchemeName)
    {
        this.fieldLayoutSchemeName = fieldLayoutSchemeName;
    }

    public String getFieldLayoutSchemeDescription()
    {
        return fieldLayoutSchemeDescription;
    }

    public void setFieldLayoutSchemeDescription(final String fieldLayoutSchemeDescription)
    {
        this.fieldLayoutSchemeDescription = fieldLayoutSchemeDescription;
    }
}
