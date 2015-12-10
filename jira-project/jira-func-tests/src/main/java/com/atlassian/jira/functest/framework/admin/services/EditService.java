package com.atlassian.jira.functest.framework.admin.services;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.admin.ViewServices;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents the functionality provided by the Edit Service Page.
 *
 * @since v4.3
 */
public class EditService implements FunctTestConstants
{
    private static final String DELAY_FORM_ELEMENT_NAME = "delay";
    private final WebTester tester;
    private final ViewServices parent;

    public EditService(final ViewServices parent, final WebTester tester)
    {
        this.tester = tester;
        this.parent = parent;
    }

    EditService setDelay(String minutes)
    {
        tester.setWorkingForm(JIRA_FORM_NAME);
        tester.setFormElement(DELAY_FORM_ELEMENT_NAME, minutes);
        return this;
    }

    ViewServices update()
    {
        tester.submit();
        return parent;
    }
}
