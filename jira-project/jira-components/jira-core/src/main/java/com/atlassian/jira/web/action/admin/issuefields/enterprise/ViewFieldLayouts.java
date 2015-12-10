package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for rendering the user interface to view all the field configurations for a JIRA instance.
 */
@WebSudoRequired
public class ViewFieldLayouts extends AbstractFieldLayoutAction
{
    private String confirm;
    private Map<Long, Collection<FieldConfigurationScheme>> fieldLayoutSchemeMap;

    public ViewFieldLayouts(final FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
        fieldLayoutSchemeMap = new HashMap<Long, Collection<FieldConfigurationScheme>>();
    }

    /**
     *  Renders a page that lists all the field configurations in JIRA.
     */
    protected String doExecute() throws Exception
    {
        return getResult();
    }

    @RequiresXsrfCheck
    public String doDeleteFieldLayout()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                if (!Boolean.valueOf(getConfirm()))
                {
                    return "confirm";
                }

                getFieldLayoutManager().deleteFieldLayout(getFieldLayout());
                return redirectToView();
            }
        }

        return getResult();
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(final String confirm)
    {
        this.confirm = confirm;
    }

    public String doEditFieldLayout()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                if (!Boolean.valueOf(getConfirm()))
                {
                    return "confirm";
                }

                getFieldLayoutManager().deleteFieldLayout(getFieldLayout());
                return redirectToView();
            }
        }

        return getResult();
    }

    public Collection<FieldConfigurationScheme> getFieldLayoutSchemes(final EditableFieldLayout editableFieldLayout)
    {
        try
        {
            if (!fieldLayoutSchemeMap.containsKey(editableFieldLayout.getId()))
            {
                fieldLayoutSchemeMap.put(editableFieldLayout.getId(), getFieldLayoutManager().getFieldConfigurationSchemes(editableFieldLayout));
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }

        return fieldLayoutSchemeMap.get(editableFieldLayout.getId());
    }
}
