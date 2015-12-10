package com.atlassian.jira.issue.link;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIssueLinkTypeDestroyer
{

    @Mock
    IssueLinkTypeManager issueLinkTypeManager;
    @Mock
    IssueLinkManager issueLinkManager;

    @Mock
    IssueLink testIssueLink1;
    @Mock
    IssueLink testIssueLink2;


    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    IssueLinkTypeDestroyerImpl destroyer;

    MockUser user;

    @Before
    public void setup()
    {
        destroyer = new IssueLinkTypeDestroyerImpl(issueLinkTypeManager, issueLinkManager);
        user = new MockUser("fred");
        when(issueLinkManager.getIssueLinks(123L)).thenReturn(ImmutableList.of(testIssueLink1, testIssueLink2));
    }

    @Test
    public void shouldRemoveIssueLinksWhenNoSwapLinkTypeGiven() throws RemoveException {
        destroyer.removeIssueLinkType(123L, null, user);
        verify(issueLinkManager).removeIssueLink(testIssueLink1, user);
        verify(issueLinkManager).removeIssueLink(testIssueLink2, user);
        verify(issueLinkTypeManager).removeIssueLinkType(123L);
    }

    @Test
    public void shouldReplaceIssueLinksWhenNoSwapLinkTypeGiven() throws RemoveException {
        IssueLinkType swapType = mock(IssueLinkType.class);
        destroyer.removeIssueLinkType(123L, swapType, user);
        verify(issueLinkManager).changeIssueLinkType(testIssueLink1, swapType, user);
        verify(issueLinkManager).changeIssueLinkType(testIssueLink1, swapType, user);
        verify(issueLinkTypeManager).removeIssueLinkType(123L);
    }

}
