package com.atlassian.jira.functest.framework.admin.user;

import net.sourceforge.jwebunit.WebTester;

/**
 * @since v6.0
 */
public class EditUserPage
{
    private final WebTester tester;

    public EditUserPage(WebTester tester)
    {
        this.tester = tester;
    }

    public EditUserPage setUsername(String username)
    {
        tester.setFormElement("username", username);
        return this;
    }

    public void submitUpdate()
    {
        tester.submit("Update");
    }
}
