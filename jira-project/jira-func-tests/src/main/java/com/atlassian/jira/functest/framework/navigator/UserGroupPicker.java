package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

/**
 * A parameter that can be used work with User/Group selector navigator components.
 *
 * @since v4.0
 */
public class UserGroupPicker implements NavigatorCondition
{
    private static final String OPTION_EMPTY = "empty";
    private static final String OPTION_CURRENT_USER = "issue_current_user";

    private final String elementName;
    private String value;

    public UserGroupPicker(String elementName)
    {
        this(elementName, null);
    }

    public UserGroupPicker(String elementName, String value)
    {
        this.elementName = elementName;
        this.value = value;
    }

    public UserGroupPicker setCurrentUser()
    {
        return setValue(OPTION_CURRENT_USER);
    }

    public UserGroupPicker setAnyUser()
    {
        return setValue(null);
    }

    public UserGroupPicker setUser(String user)
    {
        return setValue("user:" + user);
    }

    public UserGroupPicker setGroup(String group)
    {
        return setValue("group:" + group);
    }

    public UserGroupPicker setEmpty()
    {
        return setValue(OPTION_EMPTY);
    }

    public UserGroupPicker setValue(String value)
    {
        this.value = value;
        return this;
    }

    @Override
    public void parseCondition(WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        this.value = StringUtils.trimToNull(form.getParameterValue(elementName));
    }

    @Override
    public void assertSettings(Document document)
    {
        String value = document.getElementsByAttributeValue("name", elementName).get(0).val();
        value = StringUtils.trimToNull(value);
        Assert.assertEquals("Value not set correctly for element: " + elementName, this.value, value);
    }

    @Override
    public NavigatorCondition copyCondition()
    {
        return new UserGroupPicker(elementName, value);
    }

    @Override
    public NavigatorCondition copyConditionForParse()
    {
        return new UserGroupPicker(elementName);
    }
}
