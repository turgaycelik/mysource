package com.atlassian.jira.jql.permission;


import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueLiteralSanitiser
{
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private JqlIssueSupport jqlIssueSupport;
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = null;
    }

    @Test
    public void testGetIssues() throws Exception
    {
        final QueryLiteral lit1 = new QueryLiteral();
        final QueryLiteral lit2 = createLiteral("KEY");
        final QueryLiteral lit3 = createLiteral(555L);
        final QueryLiteral lit4 = createLiteral(666L);

        final Issue issue1 = new MockIssue();
        final Issue issue2 = new MockIssue();

        when(jqlIssueSupport.getIssue("KEY")).thenReturn(issue1);
        when(jqlIssueSupport.getIssue(555L)).thenReturn(issue2);
        when(jqlIssueSupport.getIssue(666L)).thenReturn(null);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser);

        final List<Issue> result1 = sanitiser.getIssues(lit1);
        assertEquals(0, result1.size());

        final List<Issue> result2 = sanitiser.getIssues(lit2);
        assertEquals(1, result2.size());
        assertSame(issue1, result2.get(0));

        final List<Issue> result3 = sanitiser.getIssues(lit3);
        assertEquals(1, result3.size());
        assertSame(issue2, result3.get(0));

        final List<Issue> result4 = sanitiser.getIssues(lit4);
        assertEquals(0, result4.size());
    }

    @Test
    public void testSanitiseLiteralsTwoLiteralsTwoIssuesAllOkay() throws Exception
    {
        final QueryLiteral lit1 = createLiteral("KEY");
        final QueryLiteral lit2 = createLiteral(555L);

        final Issue issue1 = new MockIssue(1L);
        final Issue issue2 = new MockIssue(2L);
        final Issue issue3 = new MockIssue(3L);
        final Issue issue4 = new MockIssue(4L);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue3, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue4, theUser)).thenReturn(true);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser)
        {
            @Override
            List<Issue> getIssues(final QueryLiteral literal)
            {
                if (literal == lit1)
                {
                    return CollectionBuilder.newBuilder(issue1, issue2).asList();
                }
                else if (literal == lit2)
                {
                    return CollectionBuilder.newBuilder(issue3, issue4).asList();
                }
                throw new IllegalStateException();
            }
        };

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(CollectionBuilder.newBuilder(lit1, lit2).asList());
        assertFalse(result.isModified());
    }

    @Test
    public void testSanitiseLiteralsTwoLiteralsTwoIssuesFirstLiteralOneBadOneGood() throws Exception
    {
        final QueryLiteral lit1 = createLiteral("KEY");
        final QueryLiteral lit2 = createLiteral(555L);

        final Issue issue1 = new MockIssue(1L);
        final Issue issue2 = new MockIssue(2L);
        final Issue issue3 = new MockIssue(3L);
        final Issue issue4 = new MockIssue(4L);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue3, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue4, theUser)).thenReturn(true);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser)
        {
            @Override
            List<Issue> getIssues(final QueryLiteral literal)
            {
                if (literal == lit1)
                {
                    return CollectionBuilder.newBuilder(issue1, issue2).asList();
                }
                else if (literal == lit2)
                {
                    return CollectionBuilder.newBuilder(issue3, issue4).asList();
                }
                throw new IllegalStateException();
            }
        };

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(CollectionBuilder.newBuilder(lit1, lit2).asList());
        assertFalse(result.isModified());
    }

    @Test
    public void testSanitiseLiteralsTwoLiteralsTwoIssuesFirstLiteralBothBad() throws Exception
    {
        final QueryLiteral lit1 = createLiteral("KEY");
        final QueryLiteral lit2 = createLiteral(555L);

        final Issue issue1 = new MockIssue(1L);
        final Issue issue2 = new MockIssue(2L);
        final Issue issue3 = new MockIssue(3L);
        final Issue issue4 = new MockIssue(4L);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue3, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue4, theUser)).thenReturn(true);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser)
        {
            @Override
            List<Issue> getIssues(final QueryLiteral literal)
            {
                if (literal == lit1)
                {
                    return CollectionBuilder.newBuilder(issue1, issue2).asList();
                }
                else if (literal == lit2)
                {
                    return CollectionBuilder.newBuilder(issue3, issue4).asList();
                }
                throw new IllegalStateException();
            }
        };

        final QueryLiteral expectedLit1 = createLiteral(1L);
        final QueryLiteral expectedLit2 = createLiteral(2L);
        final QueryLiteral expectedLit3 = createLiteral(555L);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(CollectionBuilder.newBuilder(lit1, lit2).asList());
        assertTrue(result.isModified());
        assertEquals(3, result.getLiterals().size());
        assertTrue(result.getLiterals().contains(expectedLit1));
        assertTrue(result.getLiterals().contains(expectedLit2));
        assertTrue(result.getLiterals().contains(expectedLit3));
    }

    @Test
    public void testSanitiseLiteralsTwoLiteralsTwoIssuesBothLiteralsOneBadOneGood() throws Exception
    {
        final QueryLiteral lit1 = createLiteral("KEY");
        final QueryLiteral lit2 = createLiteral(555L);

        final Issue issue1 = new MockIssue(1L);
        final Issue issue2 = new MockIssue(2L);
        final Issue issue3 = new MockIssue(3L);
        final Issue issue4 = new MockIssue(4L);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue3, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue4, theUser)).thenReturn(false);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser)
        {
            @Override
            List<Issue> getIssues(final QueryLiteral literal)
            {
                if (literal == lit1)
                {
                    return CollectionBuilder.newBuilder(issue1, issue2).asList();
                }
                else if (literal == lit2)
                {
                    return CollectionBuilder.newBuilder(issue3, issue4).asList();
                }
                throw new IllegalStateException();
            }
        };

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(CollectionBuilder.newBuilder(lit1, lit2).asList());
        assertFalse(result.isModified());
    }

    @Test
    public void testSanitiseLiteralsTwoLiteralsTwoIssuesAllBad() throws Exception
    {
        final QueryLiteral lit1 = createLiteral("KEY");
        final QueryLiteral lit2 = createLiteral(555L);

        final Issue issue1 = new MockIssue(1L);
        final Issue issue2 = new MockIssue(2L);
        final Issue issue3 = new MockIssue(3L);
        final Issue issue4 = new MockIssue(4L);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue3, theUser)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue4, theUser)).thenReturn(false);

        final IssueLiteralSanitiser sanitiser = new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, theUser)
        {
            @Override
            List<Issue> getIssues(final QueryLiteral literal)
            {
                if (literal == lit1)
                {
                    return CollectionBuilder.newBuilder(issue1, issue2).asList();
                }
                else if (literal == lit2)
                {
                    return CollectionBuilder.newBuilder(issue3, issue4).asList();
                }
                throw new IllegalStateException();
            }
        };

        final QueryLiteral expectedLit1 = createLiteral(1L);
        final QueryLiteral expectedLit2 = createLiteral(2L);
        final QueryLiteral expectedLit3 = createLiteral(3L);
        final QueryLiteral expectedLit4 = createLiteral(4L);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(CollectionBuilder.newBuilder(lit1, lit2).asList());
        assertTrue(result.isModified());
        assertEquals(4, result.getLiterals().size());
        assertTrue(result.getLiterals().contains(expectedLit1));
        assertTrue(result.getLiterals().contains(expectedLit2));
        assertTrue(result.getLiterals().contains(expectedLit3));
        assertTrue(result.getLiterals().contains(expectedLit4));
    }
}
