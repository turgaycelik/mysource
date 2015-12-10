package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.screen.AddFieldToScreenUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.JellyUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.opensymphony.util.TextUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

public class AddFieldToScreen extends JiraDynaBeanTagSupport
{
    private static final String SCREEN = "screen";
    private static final String FIELD_ID = "fieldId";
    private static final String TAB_POS = "tab";
    private static final String FIELD_POS = "fieldPosition";
    private static final String DEFAULT_TAB_POS = "0";

    private AddFieldToScreenUtil addFieldToScreenUtil;
    private FieldScreenManager fieldScreenManager;
    private FieldScreen fieldScreen;
    private int tabPosition = -1;

    public AddFieldToScreen()
    {
        this.addFieldToScreenUtil = ComponentAccessor.getComponentOfType(AddFieldToScreenUtil.class);
        this.fieldScreenManager = ComponentAccessor.getComponentOfType(FieldScreenManager.class);
    }

    public void doTag(XMLOutput output) throws JellyTagException
    {
        validateParams();

        addFieldToScreenUtil.setFieldScreenId(getFieldScreen().getId());
        addFieldToScreenUtil.setTabPosition(getTabPosition());
        addFieldToScreenUtil.setFieldId(getFieldId());
        addFieldToScreenUtil.setFieldPosition(getFieldPosition());
        ErrorCollection errors = addFieldToScreenUtil.validate();
        if (errors != null && errors.hasAnyErrors())
        {
            JellyUtils.processErrorCollection(errors);
        }
        addFieldToScreenUtil.execute();
    }

    private void validateParams() throws JellyTagException
    {
        if (!paramSpecified(SCREEN) || getFieldScreen() == null)
        {
            throw new MissingAttributeException(SCREEN);
        }

        if (!paramSpecified(FIELD_ID) || getFieldId() == null)
        {
            throw new MissingAttributeException(FIELD_ID);
        }

        if (!paramSpecified(TAB_POS))
        {
            getProperties().put(TAB_POS, DEFAULT_TAB_POS);
        }
        else if (getTabPosition() < 0)
        {
            throw new JellyTagException(TAB_POS);
        }

        if (!paramSpecified(FIELD_POS) || !TextUtils.stringSet(getFieldPosition()))
        {
             getProperties().put(FIELD_POS, "");
        }
    }

    private boolean paramSpecified(String paramName)
    {
        return getProperties().containsKey(paramName);
    }

    public String[] getFieldId()
    {
        return new String[] {(String) getProperties().get(FIELD_ID)};
    }

    public int getTabPosition()
    {
        if (tabPosition < 0)
        {
            try
            {
                tabPosition = Integer.parseInt((String) getProperties().get(TAB_POS));
            }
            catch (NumberFormatException nfe)
            {
                //check if input is a tab name instead
                String tabName = (String) getProperties().get(TAB_POS);
                for (FieldScreenTab tab : getFieldScreen().getTabs())
                {
                    if (tabName.equals(tab.getName()))
                    {
                        tabPosition = tab.getPosition();
                    }
                }
            }
        }

        return tabPosition;
    }

    public String getFieldPosition()
    {
        return (String) getProperties().get(FIELD_POS);
    }

    public FieldScreenTab getTab()
    {
        if (getTabPosition() > -1)
            return getFieldScreen().getTab(getTabPosition());
        else
            return null;
    }

    public FieldScreen getFieldScreen()
    {
        if (fieldScreen == null)
        {
            try
            {
                fieldScreen = fieldScreenManager.getFieldScreen(Long.decode((String) getProperties().get(SCREEN)));
            }
            catch (NumberFormatException nfe)
            {
                //cannot decode as Long, assume its a screen name not a Long id
                String screenName = (String) getProperties().get(SCREEN);
                for (FieldScreen fs : fieldScreenManager.getFieldScreens())
                {
                    if (screenName.equals(fs.getName()))
                    {
                        fieldScreen = fs;
                    }
                }
            }
        }
        return fieldScreen;
    }
}