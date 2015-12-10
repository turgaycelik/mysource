package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test system & custom fields on the edit issue page.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestEditIssueFields extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestEditIssueVersion.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }


    public void testNoneDisplayedGivenNoAvailableVersions()
    {
        navigation.issue().gotoEditIssue("MK-1");
        assertions.assertNodeHasText(versionPickerLocatorFor("versions"), "None");
        assertions.assertNodeDoesNotHaveText(versionPickerLocatorFor("versions"), "Unknown");
        assertions.assertNodeHasText(versionPickerLocatorFor("fixVersions"), "None");
        assertions.assertNodeDoesNotHaveText(versionPickerLocatorFor("fixVersions"), "Unknown");
    }

    public void testNoneDisplayedGivenNoAvailableComponents()
    {
        navigation.issue().gotoEditIssue("MK-1");
        assertions.assertNodeHasText(componentPickerLocator(), "None");
        assertions.assertNodeDoesNotHaveText(componentPickerLocator(), "Unknown");
    }

    private String versionPickerLocatorFor(String versionPickerFieldId)
    {
        return String.format("//div[contains(@class, 'field-group') and contains(@class, 'aui-field-versionspicker')]/label[@for='%s']/..", versionPickerFieldId);
    }

    private String componentPickerLocator()
    {
//        return "//div[@class='field-group aui-field-componentspicker']/span[@class='field-value']";
        return "//div[contains(@class, 'field-group') and contains(@class, 'aui-field-componentspicker')]";
    }
}
