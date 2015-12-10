package com.atlassian.jira.dev.functest.api.bc.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.functest.junit.SpringAwareTestCase;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * This is the "server" of a back end "integration" test. It assumes that JIRA is set up in a certain way, and invokes
 * APIs to test integration functionality.
 *
 * The test on the front end should have a corresponding name to this test i.e. TestIssueService.
 *
 * @since v5.0.1
 * @author mtokar
 */
public class TestIssueServiceBackEnd extends SpringAwareTestCase
{
    @Autowired
    private IssueService issueService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ChangeHistoryManager changeHistoryManager;

    @Autowired
    private HistoryMetadataManager historyMetadataManager;

    @Test
    public void testValidateUpdateWithScreenCheck() throws Exception
    {
        Issue issueWithoutField = getIssueWithoutField();
        Long issueIdWithoutField = issueWithoutField.getId();
        Issue issueWithField = getIssueWithField();
        Long issueIdWithField = issueWithField.getId();
        String updatedValue = "This is the new value";

        // assert that we don't already have this environment value set
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithoutField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithField.getEnvironment()));

        IssueInputParameters params = new IssueInputParametersImpl();
        params.setEnvironment(updatedValue);

        // try to do an update on an issue without the field -- validation passes but nothing happens
        IssueService.IssueResult updateResult = attemptUpdate(issueIdWithoutField, params);
        assertFalse("Environment field should not have been updated", updatedValue.equals(updateResult.getIssue().getEnvironment()));

        // try to do an update on an issue with the field -- validation passes and value is changed
        updateResult = attemptUpdate(issueIdWithField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());
    }

    @Test
    public void testValidateUpdateWithoutScreenCheck() throws Exception
    {
        Issue issueWithoutField = getIssueWithoutField();
        Long issueIdWithoutField = issueWithoutField.getId();
        Issue issueWithField = getIssueWithField();
        Long issueIdWithField = issueWithField.getId();
        String updatedValue = "This is the new value 2";

        // assert that we don't already have this environment value set
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithoutField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithField.getEnvironment()));

        IssueInputParameters params = new IssueInputParametersImpl();
        params.setEnvironment(updatedValue);
        params.setSkipScreenCheck(true);

        // try to do an update on an issue without the field -- validation passes and value is changed
        IssueService.IssueResult updateResult = attemptUpdate(issueIdWithoutField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());

        // try to do an update on an issue with the field -- validation passes and value is changed
        updateResult = attemptUpdate(issueIdWithField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());
    }

    @Test
    public void testValidateUpdateWithRequiredField() throws Exception
    {
        Issue issueWithoutField = getIssueWithoutField();
        Long issueIdWithoutField = issueWithoutField.getId();
        Issue issueWithField = getIssueWithField();
        Long issueIdWithField = issueWithField.getId();
        Issue issueWithRequiredField = getIssueWithRequiredField();
        Long issueIdWithRequiredField = issueWithRequiredField.getId();
        String updatedValue = "This is the new value 3";
        String oldValueWithRequiredField = issueWithRequiredField.getEnvironment();

        // assert that we don't already have this environment value set
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithoutField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(oldValueWithRequiredField));

        IssueInputParameters params = new IssueInputParametersImpl();
        params.setEnvironment(updatedValue);
        params.setSkipScreenCheck(true);

        // try to do an update on an issue without the field -- validation passes and value is changed
        IssueService.IssueResult updateResult = attemptUpdate(issueIdWithoutField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());

        // try to do an update on an issue with the field -- validation passes and value is changed
        updateResult = attemptUpdate(issueIdWithField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());

        // try to do an update on an issue with a required field which has no value -- validation fails and value is unchanged
        validateUpdateAndExpectFailure(issueIdWithRequiredField, params);
        issueWithRequiredField = getIssueWithRequiredField();
        assertEquals("Environment field should not have been updated", oldValueWithRequiredField, issueWithRequiredField.getEnvironment());
        assertFalse("Environment field should not have been updated", updatedValue.equals(issueWithRequiredField.getEnvironment()));
    }

    @Test
    public void testValidateUpdateWithRequiredFieldAndOnlyValidatingPresentFields() throws Exception
    {
        Issue issueWithoutField = getIssueWithoutField();
        Long issueIdWithoutField = issueWithoutField.getId();
        Issue issueWithField = getIssueWithField();
        Long issueIdWithField = issueWithField.getId();
        Issue issueWithRequiredField = getIssueWithRequiredField();
        Long issueIdWithRequiredField = issueWithRequiredField.getId();
        String updatedValue = "This is the new value 4";
        String oldValueWithRequiredField = issueWithRequiredField.getEnvironment();

        // assert that we don't already have this environment value set
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithoutField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(issueWithField.getEnvironment()));
        assertFalse("Environment should not be set to this value already", updatedValue.equals(oldValueWithRequiredField));

        IssueInputParameters params = new IssueInputParametersImpl();
        params.setEnvironment(updatedValue);
        params.setSkipScreenCheck(true);
        params.setRetainExistingValuesWhenParameterNotProvided(true, true);

        // try to do an update on an issue without the field -- validation passes and value is changed
        IssueService.IssueResult updateResult = attemptUpdate(issueIdWithoutField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());

        // try to do an update on an issue with the field -- validation passes and value is changed
        updateResult = attemptUpdate(issueIdWithField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());

        // try to do an update on an issue with a required field which has no value -- validation passes and value is changed
        updateResult = attemptUpdate(issueIdWithRequiredField, params);
        assertEquals("Environment field should have been updated", updatedValue, updateResult.getIssue().getEnvironment());
    }

    @Test
    public void testTransitionPersistsMetadata() throws Exception
    {
        // having
        final int startProgressAction = 4; // from EmptyJira.xml
        final HistoryMetadata historyMetadata = createHistoryMetadata();
        final IssueInputParameters inputParams = issueService.newIssueInputParameters().setHistoryMetadata(historyMetadata);
        final Issue issue = getFirstIssueFromProject("METATRANS");

        // when
        final IssueService.TransitionValidationResult validationResult = issueService.validateTransition(getUser(),
                issue.getId(), startProgressAction, inputParams);
        final IssueService.IssueResult result = issueService.transition(getUser(), validationResult);
        final List<ChangeHistory> history = changeHistoryManager.getChangeHistories(issue);
        final HistoryMetadata savedMetadata = historyMetadataManager.getHistoryMetadata(history.get(history.size() - 1),
                ApplicationUsers.from(getUser())).getHistoryMetadata();

        // then
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertThat(savedMetadata, equalTo(historyMetadata));
    }

    @Test
    public void testUpdatePersistsMetadata() throws Exception
    {
        // having
        final HistoryMetadata historyMetadata = createHistoryMetadata();
        final IssueInputParameters inputParams = issueService
                .newIssueInputParameters()
                .setSummary("newsummary")
                .setHistoryMetadata(historyMetadata);
        final Issue issue = getFirstIssueFromProject("METAUPDATE");

        // when
        final IssueService.UpdateValidationResult validationResult = issueService.validateUpdate(getUser(), issue.getId(), inputParams);
        final IssueService.IssueResult result = issueService.update(getUser(), validationResult);
        final List<ChangeHistory> history = changeHistoryManager.getChangeHistories(issue);
        final HistoryMetadata savedMetadata = historyMetadataManager.getHistoryMetadata(history.get(history.size() - 1),
                ApplicationUsers.from(getUser())).getHistoryMetadata();

        // then
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertThat(savedMetadata, equalTo(historyMetadata));
    }

    private HistoryMetadata createHistoryMetadata()
    {
        return HistoryMetadata.builder("test:issueService")
                .description("viewissue.changehistory.automatictransition")
                .actor(HistoryMetadataParticipant.builder("testUser", "user").build())
                .generator(HistoryMetadataParticipant.builder("testTransitionPersistsMetadata", "testCase").build())
                .extraData(ImmutableMap.of("extra", "data"))
                .build();
    }

    private IssueService.IssueResult attemptUpdate(Long issueId, IssueInputParameters params)
    {
        IssueService.UpdateValidationResult result = issueService.validateUpdate(getUser(), issueId, params);
        assertTrue("Validation of update should have passed: " + result.getErrorCollection(), result.isValid());
        return issueService.update(getUser(), result);
    }

    private IssueService.UpdateValidationResult validateUpdateAndExpectFailure(Long issueId, IssueInputParameters params)
    {
        IssueService.UpdateValidationResult result = issueService.validateUpdate(getUser(), issueId, params);
        assertFalse("Validation of update should have failed", result.isValid());
        return result;
    }

    private User getUser()
    {
        return userManager.getUserObject("admin");
    }

    private Issue getIssueWithoutField() throws Exception
    {
        return getFirstIssueFromProject("WithoutField");
    }

    private Issue getIssueWithField() throws Exception
    {
        return getFirstIssueFromProject("WithField");
    }

    private Issue getIssueWithRequiredField() throws Exception
    {
        return getFirstIssueFromProject("WithRequiredField");
    }

    private Issue getFirstIssueFromProject(String projectName) throws SearchException
    {
        SearchResults searchResults = searchService.search(getUser(), JqlQueryBuilder.newBuilder().where().project(projectName).buildQuery(), getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        assertTrue("Should have gotten at least one issue for project '" + projectName + "'", issues.size() > 0);
        Issue issue = issues.get(0);
        // make sure we have a full issue and not a DocumentIssue
        return issueService.getIssue(getUser(), issue.getId()).getIssue();
    }
}
