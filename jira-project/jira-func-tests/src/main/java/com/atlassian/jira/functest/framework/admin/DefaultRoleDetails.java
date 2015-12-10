package com.atlassian.jira.functest.framework.admin;

import net.sourceforge.jwebunit.WebTester;

public class DefaultRoleDetails implements RoleDetails
{
    private final WebTester tester;

    public DefaultRoleDetails(WebTester tester)
    {
        this.tester = tester;
    }

    public void setName(String name)
    {
        tester.setWorkingForm("jiraform");
        tester.setFormElement("name", name);
        tester.submit("Update");
    }

    public void setDescription(String description)
    {
        tester.setWorkingForm("jiraform");
        tester.setFormElement("description", description);
        tester.submit("Update");
    }
}