package com.atlassian.jira.action.screen;

import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

/**
 * This a loose collection of things that are used by the {@link com.atlassian.jira.jelly.tag.admin.AddFieldToScreen}
 * Jelly Tag and the {@link com.atlassian.jira.web.action.admin.issuefields.screens.ConfigureFieldScreen} action.
 * <p/>
 * This should be refactored in the future so that the setters and getters are not "reused". The validation logic should
 * live in a Service, or it could be kept here (methods should then be renamed and the class as well so that it makes
 * sense as a logical unit).
 */
public interface AddFieldToScreenUtil
{
    public ErrorCollection validate();

    public ErrorCollection execute();

    public Collection getHlFields();

    public Long getFieldScreenId();

    public void setFieldScreenId(Long fieldScreen);

    public String[] getFieldId();

    public void setFieldId(String[] fieldId);

    public int getTabPosition();

    public void setTabPosition(int tabPosition);

    public String getFieldPosition();

    public void setFieldPosition(String fieldPosition);
}
