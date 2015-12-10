package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.TimeTracking;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Arrays;

import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestTextFieldCharacterLengthValidator extends RestFuncTest
{
    private static final String DESCRIPTION_ERRORS_KEY = "description";
    private static final String ENVIRONMENT_ERRORS_KEY = "environment";

    // Use limit lower than 1000 as higher numbers are formatted with thousands separator within messages (e.g. 1000 => 1,000)
    private static final int LIMIT = 100;

    private static final String EXPECTED_WARNING_MESSAGE = "The entered text is too long. It exceeds the allowed limit of " + LIMIT + " characters.";

    final String veryLongText = createTooLongText(LIMIT);
    final String tooLongText = createTooLongText(LIMIT + 1);

    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        assertThat("expecting a text not longer than " + LIMIT + " characters", veryLongText.length(), is(not(greaterThan(LIMIT))));
        assertThat("expecting a text longer than " + LIMIT + " characters", tooLongText.length(), is(greaterThan(LIMIT)));

        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(LIMIT);
    }

    public void testDescriptionAndEnvironmentSystemField() throws Exception
    {
        runCreateIssueWithVeryLongDescriptionFieldTest();
        runCreateIssueWithTooLongDescriptionFieldTest();

        runCreateIssueWithVeryLongEnvironmentFieldTest();
        runCreateIssueWithTooLongEnvironmentFieldTest();

        runEditIssueWithVeryLongDescriptionFieldTest();
        runEditIssueWithTooLongDescriptionFieldTest();

        runEditIssueWithVeryLongEnvironmentFieldTest();
        runEditIssueWithTooLongEnvironmentFieldTest();
    }

    private void runCreateIssueWithVeryLongDescriptionFieldTest()
    {
        final IssueFields fields = createIssue().description(veryLongText);
        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);

        issueClient.loginAs("admin").create(request);
    }

    private void runCreateIssueWithTooLongDescriptionFieldTest()
    {
        final IssueFields fields = createIssue().description(tooLongText);
        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);
        final Response response = issueClient.loginAs("admin").getResponse(request);

        assertThat(response.statusCode, is(400));
        assertThat(response.entity.errors.get(DESCRIPTION_ERRORS_KEY), is(EXPECTED_WARNING_MESSAGE));
    }

    private void runCreateIssueWithVeryLongEnvironmentFieldTest()
    {
        final IssueFields fields = createIssue().environment(veryLongText);
        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);

        issueClient.loginAs("admin").create(request);
    }

    private void runCreateIssueWithTooLongEnvironmentFieldTest()
    {
        final IssueFields fields = createIssue().environment(tooLongText);
        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);
        final Response response = issueClient.loginAs("admin").getResponse(request);

        assertThat(response.statusCode, is(400));
        assertThat(response.entity.errors.get(ENVIRONMENT_ERRORS_KEY), is(EXPECTED_WARNING_MESSAGE));
    }

    private void runEditIssueWithVeryLongDescriptionFieldTest() {
        final IssueUpdateRequest updateDescriptionRequest = new IssueUpdateRequest().fields(new IssueFields()
                .description(veryLongText)
        );

        issueClient.update("TST-1", updateDescriptionRequest);
    }

    private void runEditIssueWithTooLongDescriptionFieldTest() {
        final IssueUpdateRequest updateDescriptionRequest = new IssueUpdateRequest().fields(new IssueFields()
                .description(tooLongText)
        );

        Response response = issueClient.updateResponse("TST-1", updateDescriptionRequest);
        assertThat(response.statusCode, is(400));
        assertThat(response.entity.errors.get(DESCRIPTION_ERRORS_KEY), is(EXPECTED_WARNING_MESSAGE));
    }

    private void runEditIssueWithVeryLongEnvironmentFieldTest() {
        final IssueUpdateRequest updateEnvironmentRequest = new IssueUpdateRequest().fields(new IssueFields()
                .environment(veryLongText)
        );

        issueClient.update("TST-1", updateEnvironmentRequest);
    }

    private void runEditIssueWithTooLongEnvironmentFieldTest() {
        final IssueUpdateRequest updateEnvironmentRequest = new IssueUpdateRequest().fields(new IssueFields()
                .environment(tooLongText)
        );

        Response response = issueClient.updateResponse("TST-1", updateEnvironmentRequest);
        assertThat(response.statusCode, is(400));
        assertThat(response.entity.errors.get(ENVIRONMENT_ERRORS_KEY), is(EXPECTED_WARNING_MESSAGE));
    }

    private IssueFields createIssue()
    {
        final IssueFields fields = new IssueFields();
        fields.project(withId("10000"));
        fields.issueType(withId("1"));  // Bug
        fields.summary("my first fields");
        fields.description("description");
        fields.fixVersions(withId("10001"));
        fields.components(withId("10000"));
        fields.labels(Arrays.asList("abc", "def"));
        fields.dueDate("2011-03-01");
        fields.versions(withId("10000"));
        fields.environment("environment");
        fields.timeTracking(new TimeTracking("2h", null));
        return fields;
    }

    private static String createTooLongText(final int numberOfCharacters)
    {
        return RandomStringUtils.random(numberOfCharacters, "abcdefghijklmnopqrstuvwxyz ".toCharArray());
    }

}
