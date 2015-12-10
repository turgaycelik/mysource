package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestIssueLink
{
    private GenericValue issueLinkTypeGV;
    private GenericValue sourceIssue;
    private GenericValue destinationIssue;
    private GenericValue issueLinkGV;
    private IssueLink issueLink;
    private IssueLinkType issueLinkType;
    @Mock private IssueLinkTypeManager mockIssueLinkTypeManager;
    @Mock private IssueManager mockIssueManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(OfBizDelegator.class, new MockOfBizDelegator());
        issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType",
                EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward", "style", "test style"));
        issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        sourceIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test source summary", "key", "TST-1"));
        destinationIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test destination summary", "key", "TST-2"));
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    private void setupIssueLink(IssueLinkTypeManager issueLinkTypeManager, IssueManager issueManager)
    {
        issueLinkGV = UtilsForTests.getTestEntity("IssueLink",
                EasyMap.build("source", sourceIssue.getLong("id"), "destination", destinationIssue.getLong("id"),
                        "linktype", issueLinkTypeGV.getLong("id"), "sequence", new Long(0)));
        issueLink = new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, issueManager);
    }

    @Test
    public void testConstructor()
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));

        try
        {
            new IssueLinkImpl(issue, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Entity must be an 'IssueLink', not '" + issue.getEntityName() + "'.", e.getMessage());
        }
    }

    @Test
    public void testGetters()
    {
        when(mockIssueLinkTypeManager.getIssueLinkType(issueLinkTypeGV.getLong("id"))).thenReturn(issueLinkType);
        setupIssueLink(mockIssueLinkTypeManager, mockIssueManager);

        assertEquals(issueLinkGV.getLong("id"), issueLink.getId());
        assertEquals(sourceIssue.getLong("id"), issueLink.getSourceId());
        assertEquals(destinationIssue.getLong("id"), issueLink.getDestinationId());
        assertEquals(issueLinkTypeGV.getLong("id"), issueLink.getLinkTypeId());
        assertEquals(issueLinkGV.getLong("sequence"), issueLink.getSequence());
        assertEquals(issueLinkType, issueLink.getIssueLinkType());
    }
}
