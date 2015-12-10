package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.RandomStringUtils;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @since v5.0.3
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestTextFieldCharacterLengthValidator extends FuncTestCase
{
    private static final String DESCRIPTION_FORM_ELEMENT_NAME = "description";
    private static final String ENVIRONMENT_FORM_ELEMENT_NAME = "environment";
    // Use limit lower than 1000 as higher numbers are formatted with thousands separator within messages (e.g. 1000 => 1,000)
    private static final int LIMIT = 100;

    private static final String EXPECTED_WARNING_MESSAGE = "The entered text is too long. It exceeds the allowed limit of " + LIMIT + " characters.";

    private final String largeText = createTooLongText(LIMIT);
    private final String tooLargeText = createTooLongText(LIMIT + 1);

    protected void setUpTest()
    {
        assertThat("expecting a text not longer than " + LIMIT + " characters", largeText.length(), is(not(greaterThan(LIMIT))));
        assertThat("expecting a text longer than " + LIMIT + " characters", tooLargeText.length(), is(greaterThan(LIMIT)));

        administration.restoreData("TestEditIssue.xml");
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(LIMIT);
    }

    public void testDescriptionAndEnvironmentSystemField() throws Exception
    {
        runCreateIssueWithVeryLongDescriptionTextTest();
        runCreateIssueWithTooLongDescriptionTextTest();

        runCreateIssueWithVeryLongEnvironmentTextTest();
        runCreateIssueWithTooLongEnvironmentTextTest();

        runEditIssueWithVeryLongDescriptionTextTest();
        runEditIssueWithTooLongDescriptionTextTest();

        runEditIssueWithVeryLongEnvironmentTextTest();
        runEditIssueWithTooLongEnvironmentTextTest();
    }

    private void runCreateIssueWithVeryLongDescriptionTextTest()
    {
        createIssue("Issue with very long description text", DESCRIPTION_FORM_ELEMENT_NAME, largeText);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
    }

    private void runCreateIssueWithTooLongDescriptionTextTest()
    {
        createIssue("Issue with too long description text", DESCRIPTION_FORM_ELEMENT_NAME, tooLargeText);
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(EXPECTED_WARNING_MESSAGE);
    }

    private void runCreateIssueWithVeryLongEnvironmentTextTest()
    {
        createIssue("Issue with very long environment text", ENVIRONMENT_FORM_ELEMENT_NAME, largeText);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
    }

    private void runCreateIssueWithTooLongEnvironmentTextTest()
    {
        createIssue("Issue with too long environment text", ENVIRONMENT_FORM_ELEMENT_NAME, tooLargeText);
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(EXPECTED_WARNING_MESSAGE);
    }

    private void runEditIssueWithVeryLongDescriptionTextTest()
    {
        editIssue(DESCRIPTION_FORM_ELEMENT_NAME, largeText);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
    }

    private void runEditIssueWithTooLongDescriptionTextTest()
    {
        editIssue(DESCRIPTION_FORM_ELEMENT_NAME, tooLargeText);
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(EXPECTED_WARNING_MESSAGE);
    }

    private void runEditIssueWithVeryLongEnvironmentTextTest()
    {
        editIssue(ENVIRONMENT_FORM_ELEMENT_NAME, largeText);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
    }

    private void runEditIssueWithTooLongEnvironmentTextTest()
    {
        editIssue(ENVIRONMENT_FORM_ELEMENT_NAME, tooLargeText);
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(EXPECTED_WARNING_MESSAGE);
    }

    private void createIssue(final String summaryValue, final String formElementName, final String formElementValue)
    {
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        tester.setFormElement("summary", summaryValue);
        tester.setFormElement(formElementName, formElementValue);
        tester.submit("Create");
    }

    private void editIssue(String formElementName, String formElementValue)
    {
        navigation.issue().gotoEditIssue("HSP-1");
        tester.setFormElement(formElementName, formElementValue);
        tester.submit();
    }

    private static String createTooLongText(final int numberOfCharacters)
    {
        return RandomStringUtils.random(numberOfCharacters, "abcdefghijklmnopqrstuvwxyz ".toCharArray());
    }

}
