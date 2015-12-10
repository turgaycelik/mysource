package com.atlassian.jira.web.action.issue.bulkedit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;

public class SingleIssueModeInitializerTest
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private IssueManager issueManagerMock;

    @Mock
    private MutableIssue issueMock;

    private BulkEditBean bulkEditBean;

    @Before
    public void setUp()
    {
        when(issueMock.getId()).thenReturn(12345L);
        when(issueMock.getKey()).thenReturn("KEY-1");
        when(issueManagerMock.getIssueObject(12345L)).thenReturn(issueMock);

        bulkEditBean = new BulkEditBeanImpl(issueManagerMock);
    }

    @Test
    public void testInitialize()
    {
        SingleIssueModeInitializer.initialize(bulkEditBean, issueMock);
        assertThat(bulkEditBean.getSelectedIssues(), Matchers.<Issue>hasItems(issueMock));
        assertThat(bulkEditBean.getIssuesFromSearchRequest(), Matchers.<Issue>hasItems(issueMock));
        assertThat(bulkEditBean.getSingleIssueKey(), is(equalTo("KEY-1")));
    }

}
