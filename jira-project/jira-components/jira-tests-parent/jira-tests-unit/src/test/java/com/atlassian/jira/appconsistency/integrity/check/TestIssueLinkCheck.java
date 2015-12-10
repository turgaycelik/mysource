package com.atlassian.jira.appconsistency.integrity.check;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestIssueLinkCheck
{
    private static final int MAX_VALID_ISSUE_ID = 3;

    private List<GenericValue> mockDB = new ArrayList<GenericValue>();
    private List<GenericValue> validIssues;
    private GenericValue validIssueLink;
    @Mock private I18nHelper mockI18nHelper;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        final JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        new MockComponentWorker().init()
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator());

        //setup mock db
        //add issues
        validIssues = makeMockIssues();
        mockDB.addAll(validIssues);
        //add valid issue link
        validIssueLink = makeMockIssueLink(1, 1, 2);
        mockDB.add(validIssueLink);
        //add corrupt issue links
        mockDB.add(makeMockIssueLink(101, 1001, 2));
        mockDB.add(makeMockIssueLink(102, 2, 1001));
        mockDB.add(makeMockIssueLink(103, 1001, 1001));
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testPreview() throws IntegrityException
    {
        final List<GenericValue> expectedDB = mockDB;

        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(mockDB, expectedDB);

        IssueLinkCheck check = new IssueLinkCheck(mockOfBizDelegator, 0);
        List amendments = check.preview();

        //check that only corrupt issue links are returned
        assertEquals(3, amendments.size());
        assertEquals("101", (getAmendmentValue((DeleteEntityAmendment) amendments.get(0), "id")));
        assertEquals("102", (getAmendmentValue((DeleteEntityAmendment) amendments.get(1), "id")));
        assertEquals("103", (getAmendmentValue((DeleteEntityAmendment) amendments.get(2), "id")));

        //verify the expected db (no changes)
        mockOfBizDelegator.verifyAll();
    }

    @Test
    public void testCorrect() throws IntegrityException
    {
        //setup expected db
        final List<GenericValue> expectedDB = new ArrayList<GenericValue>();
        expectedDB.addAll(validIssues);
        expectedDB.add(validIssueLink);

        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(mockDB, expectedDB);

        IssueLinkCheck check = new IssueLinkCheck(mockOfBizDelegator, 0);
        List amendments = check.correct();

        //check that only corrupt issue links are returned
        assertEquals(3, amendments.size());
        assertEquals("101", (getAmendmentValue((DeleteEntityAmendment) amendments.get(0), "id")));
        assertEquals("102", (getAmendmentValue((DeleteEntityAmendment) amendments.get(1), "id")));
        assertEquals("103", (getAmendmentValue((DeleteEntityAmendment) amendments.get(2), "id")));

        //verify the expected db (has changes)
        mockOfBizDelegator.verifyAll();

        //check if we run the preview/correct again, no corrupt issue links are found
        amendments = check.preview();
        assertTrue(amendments.isEmpty());
        amendments = check.correct();
        assertTrue(amendments.isEmpty());
    }

    private String getAmendmentValue(DeleteEntityAmendment amendment, String field)
    {
        return amendment.getEntity().getString(field);
    }

    private GenericValue makeMockIssueLink(long id, long source, long destination)
    {
        return UtilsForTests.getTestEntity(
                "IssueLink", EasyMap.build("id", id, "source", new Long(source), "destination", destination));
    }

    private List<GenericValue> makeMockIssues()
    {
        List<GenericValue> issues = new ArrayList<GenericValue>();
        for (int i = 0; i < MAX_VALID_ISSUE_ID; i++)
        {
            issues.add(UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(i))));
        }
        return issues;
    }
}
