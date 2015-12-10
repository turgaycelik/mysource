package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import org.junit.Test;


/*
 This test needs the "test.jira.setup.skip" property set to true.
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION_SETUP } )
public class TestVisualRegressionSetup extends JiraVisualRegressionTest
{

    @Test
    @LoginAs (anonymous = true) // prevent attempt to log in.
    public void checkDbSetupForVisualRegression()
    {
        goTo("/secure/SetupDatabase!default.jspa");
        assertUIMatches("setup-db");
    }


}
