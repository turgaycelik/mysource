package com.atlassian.jira.issue.fields.rest;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;

import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith (ListeningMockitoRunner.class)
public class IssueLinksRestFieldOperationsHandlerTest
{
    NoopI18nHelper i18nHelper;

    @Mock
    IssueContext issueCtx;

    @Mock
    Issue issue;

    @Mock
    IssueLinkTypeFinder issueLinkTypeFinder;

    @Mock
    IssueFinder issueFinder;

    @Test
    public void updateIssueInputParametersShouldNotAllowMoreThanOneLinkOperation() throws Exception
    {
        String fieldId = IssueFieldConstants.ISSUE_LINKS;
        IssueInputParameters inputParameters = new IssueInputParametersImpl();
        List<FieldOperationHolder> operations = Lists.newArrayList(
                new FieldOperationHolder("add", new JsonData(new Object())),
                new FieldOperationHolder("add", new JsonData(new Object()))
        );
        
        ErrorCollection errors = new IssueLinksRestFieldOperationsHandler(i18nHelper, issueLinkTypeFinder, issueFinder).updateIssueInputParameters(issueCtx, issue, fieldId, inputParameters, operations);
        assertThat(errors.getErrors(), Matchers.hasEntry("issuelinks", NoopI18nHelper.makeTranslation("rest.operations.morethanone", String.valueOf(operations.size()), IssueFieldConstants.ISSUE_LINKS)));
    }

    @Test
    public void getSupportedOperationsShouldOnlyListAdd() throws Exception
    {
        RestFieldOperationsHandler handler = new IssueLinksRestFieldOperationsHandler(i18nHelper, issueLinkTypeFinder, issueFinder);
        assertThat(handler.getSupportedOperations(), equalTo(Collections.singleton("add")));
    }

    @Before
    public void setUp() throws Exception
    {
        i18nHelper = new NoopI18nHelper();
    }
}
