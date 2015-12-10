package com.atlassian.jira.action.screen;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.LinkedList;

public class AddFieldToScreenUtilImpl implements AddFieldToScreenUtil
{
    int fieldPos = -1;
    private Collection hlFields;
    private FieldScreen fieldScreen;

    private Long fieldScreenId;
    private String[] fieldId;
    private int tabPosition;
    private String fieldPosition;

    private final FieldManager fieldManager;
    private final FieldScreenManager fieldScreenManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public AddFieldToScreenUtilImpl(final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldManager fieldManager, final FieldScreenManager fieldScreenManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.fieldManager = fieldManager;
        this.fieldScreenManager = fieldScreenManager;
        hlFields = new LinkedList();
    }

    public ErrorCollection validate()
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (getFieldScreen() == null)
        {
            errorCollection.addErrorMessage(jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.field.screen"));
        }

        if (TextUtils.stringSet(fieldPosition))
        {
            try
            {
                fieldPos = Integer.parseInt(fieldPosition) - 1;
                // Note ">" and not ">=" is used in the comparison below to allow users to specify the next largest position
                if (fieldPos < 0 || fieldPos > getTab().getFieldScreenLayoutItems().size())
                {
                    errorCollection.addError("fieldPosition", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.field.position"));
                }
            }
            catch (NumberFormatException e)
            {
                errorCollection.addError("fieldPosition", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.field.position"));
            }
        }
        else
        {
            fieldPos = getTab().getFieldScreenLayoutItems().size();
        }


        if (fieldId == null || fieldId.length <= 0)
        {
            errorCollection.addError("fieldId", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.field"));
        }

        if (!errorCollection.hasAnyErrors())
        {
            for (int i = fieldId.length - 1; i >= 0; i--)
            {
                // Ensure that the fields do not exist on the field screen
                if (getFieldScreen().containsField(fieldId[i]))
                {
                    errorCollection.addError("fieldId", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.field.with.id.already.exists", fieldId[i]));
                }
                else // Field is not on the screen, but check that such fields exist
                {
                    String fieldid = fieldId[i];
                    try
                    {
                        Field field = fieldManager.getField(fieldid);
                        if (field == null)
                        {
                            errorCollection.addError("fieldId", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.field.id", fieldId[i]));
                        }
                    }
                    catch (IllegalArgumentException iae)
                    {
                        //invalid customfield
                        errorCollection.addError("fieldId", jiraAuthenticationContext.getI18nHelper().getText("admin.errors.invalid.custom.field.id", fieldId[i]));
                    }
                }
            }
        }

        return errorCollection;
    }

    private FieldScreen getFieldScreen()
    {
        if (fieldScreen == null && fieldScreenId != null)
        {
            fieldScreen = fieldScreenManager.getFieldScreen(fieldScreenId);
        }

        return fieldScreen;
    }

    public ErrorCollection execute()
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        // If the tab has any fields on it, then highlight the fields that are being added
        boolean highlightFields = !getTab().getFieldScreenLayoutItems().isEmpty();
        // Go through the array backwards so that the fields are added in the order they appear in the listbox on the page
        for (int i = fieldId.length - 1; i >= 0; i--)
        {
            getTab().addFieldScreenLayoutItem(fieldId[i], fieldPos);
            if (highlightFields)
            {
                hlFields.add(fieldId[i]);
            }
        }

        return errorCollection;
    }

    public FieldScreenTab getTab()
    {
        if (getTabPosition() > -1)
        {
            return getFieldScreen().getTab(getTabPosition());
        }
        else
        {
            return null;
        }
    }

    public Collection getHlFields()
    {
        return hlFields;
    }

    public Long getFieldScreenId()
    {
        return fieldScreenId;
    }

    public void setFieldScreenId(Long fieldScreenId)
    {
        this.fieldScreenId = fieldScreenId;
    }

    public String[] getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String[] fieldId)
    {
        this.fieldId = fieldId;
    }

    public int getTabPosition()
    {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition)
    {
        this.tabPosition = tabPosition;
    }

    public String getFieldPosition()
    {
        return fieldPosition;
    }

    public void setFieldPosition(String fieldPosition)
    {
        this.fieldPosition = fieldPosition;
    }
}
