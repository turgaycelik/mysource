package com.atlassian.jira.issue.fields.rest;

import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Responsible for holding unit tests for {@link IssueFinderImpl}
 *
 * @since v6.2.4
 */
public class TestIssueFinderImpl
{
    @Test
    public void findShouldReturnANullReferenceForANullIssueIdOrKey()
    {
        final IssueFinder issueFinder = new IssueFinderImpl
                (
                        new MockSimpleAuthenticationContext(ImmutableUser.newUser().name("admin").toUser()),
                        mock(IssueManager.class), mock(PermissionManager.class)
                );

        assertEquals(issueFinder.findIssue((String) null, new SimpleErrorCollection()), null);
    }

    @Test
    public void findShouldAddAnErrorCollectionMessageForANullIssueIdOrKey()
    {
        final IssueFinder issueFinder = new IssueFinderImpl
                (
                        new MockSimpleAuthenticationContext(ImmutableUser.newUser().name("admin").toUser()),
                        mock(IssueManager.class), mock(PermissionManager.class)
                );

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        issueFinder.findIssue((String) null, errorCollection);

        assertTrue(errorCollection.hasAnyErrors());
    }
}
