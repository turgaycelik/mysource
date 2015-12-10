package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
public class LocalIssueLinkUtilsTest
{
    @Mock
    IssueLinkType linkType;

    @Mock
    FieldVisibilityManager fieldVisibilityManager;

    @Test
    public void convertToIssueLinkContextsShouldHandleIssuesWithNoPriority() throws Exception
    {
        Status status = mock(Status.class);
        when(status.getGenericValue()).thenReturn(new MockGenericValue("status"));
        when(status.getIconUrl()).thenReturn("http://lo/123.gif");

        Issue issueWithNoPrio = mock(Issue.class);
        when(issueWithNoPrio.getIssueTypeObject()).thenReturn(new MockIssueType("1", "Bug", false));
        when(issueWithNoPrio.getStatusObject()).thenReturn(status);

        LinkCollection links = Mockito.mock(LinkCollection.class);
        when(links.getLinkTypes()).thenReturn(Collections.singleton(linkType));
        when(links.getOutwardIssues(linkType.getName())).thenReturn(Arrays.<Issue>asList(issueWithNoPrio));

        Map<String, List<IssueLinkContext>> contexts = LocalIssueLinkUtils.convertToIssueLinkContexts(links, 123L, "http://nasa.gov", fieldVisibilityManager);
        assertThat(contexts.size(), equalTo(1));
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(linkType.getId()).thenReturn(40L);
        when(linkType.getName()).thenReturn("betterness");
        when(linkType.getInward()).thenReturn("better than");
        when(linkType.getOutward()).thenReturn("worse than");
    }
}
