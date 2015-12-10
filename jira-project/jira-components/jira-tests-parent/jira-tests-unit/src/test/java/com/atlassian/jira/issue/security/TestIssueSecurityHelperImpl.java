package com.atlassian.jira.issue.security;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test IssueSecurityHelperImpl
 *
 * @since v3.13
 */
public class TestIssueSecurityHelperImpl
{
    private IssueSecurityHelperImpl issueSecurityHelperImpl;
    private MutableIssue sourceIssue;
    private MutableIssue targetIssue;
    private Mock mockOrderableFieldControl;

    @Before
    public void setUp()
    {
        mockOrderableFieldControl = new Mock(OrderableField.class);

        Mock mockFieldLayoutItem = new Mock(FieldLayoutItem.class);
        mockFieldLayoutItem.expectAndReturn("getOrderableField", mockOrderableFieldControl.proxy());

        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.expectAndReturn("getFieldLayoutItem", new Constraint[] { P.eq("security") }, mockFieldLayoutItem.proxy());

        final Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.expectAndReturn("getFieldLayout", P.ANY_ARGS, mockFieldLayout.proxy());

        issueSecurityHelperImpl = new IssueSecurityHelperImpl((FieldLayoutManager) mockFieldLayoutManager.proxy());

        // Set up the original values for the issue
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(new Long(1)));

        MockGenericValue mockIssueTypeGv = new MockGenericValue("IssueType", EasyMap.build("id", new Long(1)));
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addIssueType(mockIssueTypeGv);

        MockIssueFactory.setProjectManager(mockProjectManager);
        MockIssueFactory.setConstantsManager(mockConstantsManager);

        sourceIssue = MockIssueFactory.createIssue(1);
        sourceIssue.setProjectId(new Long(1));
        sourceIssue.setIssueType(mockIssueTypeGv);
        sourceIssue.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10))));

        // First test is when the new Project will stay the same.
        targetIssue = MockIssueFactory.createIssue(1);
        targetIssue.setProjectId(new Long(1));
        targetIssue.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10))));
    }

    @Test
    public void testSecurityLevelDoesntNeedMove()
    {
        mockOrderableFieldControl.expectAndReturn("needsMove", P.ANY_ARGS, new MessagedResult(false));

        assertFalse(issueSecurityHelperImpl.securityLevelNeedsMove(sourceIssue, targetIssue));
    }

    @Test
    public void testSecurityLevelNeedsMove()
    {
        mockOrderableFieldControl.expectAndReturn("needsMove", P.ANY_ARGS, new MessagedResult(true));

        assertTrue(issueSecurityHelperImpl.securityLevelNeedsMove(sourceIssue, targetIssue));
    }

    @Test
    public void testNullFieldLayoutItem()
    {
        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.expectAndReturn("getFieldLayoutItem", new Constraint[] { P.eq("security") }, null);
        final Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.expectAndReturn("getFieldLayout", P.ANY_ARGS, mockFieldLayout.proxy());
        issueSecurityHelperImpl = new IssueSecurityHelperImpl((FieldLayoutManager) mockFieldLayoutManager.proxy());

        assertFalse(issueSecurityHelperImpl.securityLevelNeedsMove(sourceIssue, targetIssue));
    }

    @Test
    public void testGetFieldLayoutThrowsException()
    {
        final Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
        mockFieldLayoutManager.expectAndThrow("getFieldLayout", P.ANY_ARGS, new DataAccessException("blah"));
        issueSecurityHelperImpl = new IssueSecurityHelperImpl((FieldLayoutManager) mockFieldLayoutManager.proxy());

        try
        {
            issueSecurityHelperImpl.securityLevelNeedsMove(sourceIssue, targetIssue);
            fail("DataAccessException expected.");
        }
        catch (DataAccessException e)
        {
            // expected.
        }
    }
}
