package com.atlassian.jira.webtests.ztests.customfield.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for verifying that editing a custom field's default value is not susceptible to xsrf attacks.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS })
public class TestXsrfEditCustomFieldDefaultValue extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestXsrfEditCustomFieldDefaultValue.xml");
    }

    /**
     * Performs standard check using the {@link com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck}
     * func-test framework classes.
     * @throws Exception An unexpected error.
     */
    public void testWhenSubmittingADodgyTokenAndAValidToken() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Edit Custom Field Default Value",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("config_customfield_10000");
                                tester.clickLink("customfield_10000-edit-default");
                                tester.setFormElement("customfield_10000", FRED_USERNAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Set Default"))
        ).run(funcTestHelperFactory);
    }
}
